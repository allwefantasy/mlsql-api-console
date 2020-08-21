package tech.mlsql.api.controller

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest._
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.model.{MlsqlBackendProxy, MlsqlUser}
import tech.mlsql.quill_model.{MlsqlApply, MlsqlDs, MlsqlEngine, MlsqlJob}
import tech.mlsql.service.{EngineService, QuillScriptFileService, RestService, UserService}
import tech.mlsql.utils.JSONTool

import scala.collection.JavaConverters._


class ClusterProxyController extends ApplicationController with AuthModule with Logging {

  val clusterUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url
  val engineUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_engine_url

  @At(path = Array("/api_v1/run/script"), types = Array(Method.POST))
  def runScript = {
    tokenAuth()
    val engineName = if(param("engineName")=="undefined" || !hasParam("engineName")){
       UserService.getBackendName(user).getOrElse("")
    }  else param("engineName")
    val engineConfigOpt = EngineService.findByName(engineName)
    
    val _proxyUrl = if (clusterUrl != null && !clusterUrl.isEmpty) clusterUrl else engineUrl
    val _myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }
    val _home = s"${MLSQLConsoleCommandConfig.commandConfig.user_home}"
    val _skipAuth = if (!MLSQLConsoleCommandConfig.commandConfig.enable_auth_center) MlsqlEngine.SKIP_AUTH else MlsqlEngine.AUTH

    val engineConfig = engineConfigOpt match {
      case Some(engineConfig) => engineConfig
      case None => EngineService.list().headOption.getOrElse(MlsqlEngine(
        0, "", _proxyUrl, _home, _myUrl, _myUrl, _myUrl, _skipAuth,"{}",""
      ))
    }


    val proxy = RestService.client(engineConfig.url)
    var newparams = params().asScala.toMap

    val quileFileService = findService(classOf[QuillScriptFileService])
    val sql = newparams.getOrElse("runMode", "mlsql") match {
      case "python" =>
        val scriptId = newparams("scriptId").toInt
        val projectName = quileFileService.findProjectNameFileIn(scriptId)
        val buffer = quileFileService.findProjectFiles(user.name, projectName).toList

        val currentScriptFile = buffer.filter(_.scriptFile.id == scriptId).head

        //currentScriptFile is in a package?

        val isInPackage = quileFileService.isInPackage(currentScriptFile, buffer)
        if (isInPackage) {
          JSONTool.toJsonStr(buffer)
        } else {
          JSONTool.toJsonStr(List(currentScriptFile))
        }
      case _ => newparams("sql")
    }

    val tagsMap = try {
      JSONTool.parseJson[Map[String, String]](user.backendTags)
    } catch {
      case e: Exception => Map(MlsqlUser.NORMAL_TAG_TYPE -> user.backendTags)
    }


    var tags = if (sql.contains("!scheduler")) {
      tagsMap.get(MlsqlUser.SCHEDULER_TAG_TYPE).getOrElse("")
    } else {
      tagsMap.get(MlsqlUser.NORMAL_TAG_TYPE).getOrElse("")
    }

    if (tags == null) {
      tags = ""
    }

    newparams += ("context.__default__include_fetch_url__" -> s"${engineConfig.consoleUrl}/api_v1/script_file/include")
    newparams += ("context.__default__console_url__" -> s"${engineConfig.consoleUrl}")
    newparams += ("context.__default__fileserver_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/download")
    newparams += ("context.__default__fileserver_upload_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/upload")
    newparams += ("context.__auth_client__" -> s"streaming.dsl.auth.meta.client.MLSQLConsoleClient")
    newparams += ("context.__auth_server_url__" -> s"${engineConfig.authServerUrl}/api_v1/table/auth")
    newparams += ("context.__auth_secret__" -> RestService.auth_secret)
    newparams += ("tags" -> tags)
    newparams += ("access_token" -> engineConfig.accessToken)
    newparams += ("defaultPathPrefix" -> PathFun(engineConfig.home).add(user.name).toPath)
    newparams += ("skipAuth" -> (MlsqlEngine.SKIP_AUTH == engineConfig.skipAuth).toString)
    newparams += ("skipGrammarValidate" -> "false")
    if(!hasParam("callback")){
      newparams += ("callback" -> s"${engineConfig.consoleUrl}/api_v1/job/callback?__auth_secret__=${RestService.auth_secret}")
    }
    newparams += ("sql" -> sql)
    newparams += ("owner" -> user.name)

    if(!hasParam("schemaInferUrl")){
       newparams += ("schemaInferUrl" -> (engineConfig.url+"/run/script"))
    }

    def cleanSql(sql:String)={
      try {
        sql.split("\n").filterNot(line=>line.contains("password")).mkString("\n")
      }catch {
        case e:Exception=>
          sql
      }
    }

    logInfo(cleanSql(sql))
    
    if (hasParam("__connect__")) {
      val connect = MlsqlDs.getConnect(param("__connect__"), user)
      val newSql = connect + sql
      newparams += ("sql" -> newSql)
    }

    def buildJob(status: Int, reason: String) = {
      val job = MlsqlJob(0, newparams("jobName"), sql, status, user.id, reason, System.currentTimeMillis(), -1, "[]")
      job
    }

    val isSaveQuery = newparams.getOrElse("queryType", "human") == "human"

    if (newparams.getOrElse("queryType", "human") == "analysis_workshop_apply_action") {
      newparams += ("timeout" -> (user.apply_timeout * 1000).toString)
    }

    val isAsync = newparams.getOrElse("async", "false").toBoolean
    val startTime = System.currentTimeMillis()
    val response = proxy.runScript(newparams)
    if (response.getStatus == -1) {
      val msg = genErrorMessage(s"Request ${response.getUrl} [${response.getContent}]. Please check the backend is alive.")
      if (isSaveQuery) {
        ctx.run(query[MlsqlJob].insert(lift(buildJob(MlsqlJob.FAIL, msg))))
      }
      render(500, msg)
    }

    if (newparams.getOrElse("queryType", "human") == "analysis_workshop_apply_action") {
      ctx.run(query[MlsqlApply].insert(
        _.name -> lift(param("analysis_workshop_table_name")),
        _.mlsqlUserId -> lift(user.id),
        _.content -> lift(sql),
        _.response -> lift(response.getContent),
        _.createdAt -> lift(startTime),
        _.finishAt -> lift(System.currentTimeMillis()),
        _.status -> lift(response.getStatus),
        _.applySql -> lift(param("analysis_workshop_sql"))
      ))
    }

    if (isSaveQuery) {
      ctx.run(query[MlsqlJob].insert(lift(buildJob(MlsqlJob.RUNNING, ""))))
    }
    if (!isAsync && newparams.contains("jobName")) {
      ctx.run(query[MlsqlJob].filter(_.name == lift(newparams("jobName"))).
        update(_.status -> lift(MlsqlJob.SUCCESS),
          _.response -> lift(response.getContent),
          _.finishAt -> lift(System.currentTimeMillis())))
    }
    render(response.getStatus, response.getContent)
  }

  def genErrorMessage(msg: String) = {
    JSONTool.toJsonStr(List(Map("msg" -> msg)))
  }

  @At(path = Array("/api_v1/cluster"), types = Array(Method.POST))
  def clusterManager = {
    tokenAuth()
    if (clusterUrl == null) {
      render(403, genErrorMessage("mlsql_cluster_url is not configured. You cannot visit the cluster functions "))
    }
    val proxy = RestService.client(MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url)
    var newparams = params().asScala.toMap
    val myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }
    newparams += ("context.__default__include_fetch_url__" -> s"${myUrl}/api_v1/script_file/include")
    newparams -= "teamName"
    val response = param("action") match {
      case "/backend/list" => proxy.backendList(newparams)
      case "/backend/add" =>
        val tmpRes = proxy.backendAdd(newparams)
        if (tmpRes.getStatus == 200) {
          MlsqlBackendProxy.build(param("teamName"), param("name"))
        }
        tmpRes
      case "/backend/remove" =>
        MlsqlBackendProxy.findByName(param("name")).delete()
        proxy.backendRemove(newparams)
      case "/backend/tags/update" => proxy.backendTagsUpdate(newparams)
      case "/backend/name/check" => proxy.backendNameCheck(newparams)
      case "/backend/list/names" => proxy.backendListNames(newparams)
    }

    render(response.getStatus, response.getContent)
  }

}
