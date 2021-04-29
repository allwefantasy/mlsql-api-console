package tech.mlsql.service

import java.util.UUID

import net.csdn.ServiceFramwork
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.transport.HttpTransportService.SResponse
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer.IndexOptimizer
import tech.mlsql.quill_model.{MlsqlDs, MlsqlEngine, MlsqlJob, MlsqlUser}
import tech.mlsql.service.notebook.hint.{DeployModelHint, DeployPythonModelHint, KylinHint, PythonHint}

import scala.collection.mutable

/**
 * 8/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class RunScript(user: MlsqlUser, _params: Map[String, String]) extends Logging {

  private val clusterUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url
  private val engineUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_engine_url
  private val extraParams = mutable.HashMap[String, String]()

  def sql(sql: String) = {
    extraParams += ("sql" -> sql)
    this
  }

  def owner(owner: String) = {
    extraParams += ("owner" -> owner)
    this
  }

  def async(async: Boolean) = {
    extraParams += ("async" -> async.toString)
    this
  }

  def timeout(timeout: Long) = {
    extraParams += ("timeout" -> timeout.toString)
    this
  }

  def executeMode(executeMode: String) = {
    extraParams += ("executeMode" -> executeMode)
    this
  }

  def jobName(jobName: String) = {
    extraParams += ("jobName" -> jobName)
    this
  }

  def engineName(engineName: String) = {
    extraParams += ("engineName" -> engineName)
    this
  }

  private def param(str: String) = {
    params().getOrElse(str, null)
  }

  private def hasParam(str: String) = {
    params().contains(str)
  }

  private def params() = {
    _params ++ extraParams
  }

  private def genErrorMessage(msg: String) = {
    JSONTool.toJsonStr(List(Map("msg" -> msg)))
  }

  def buildFailRecord(resp: RunScriptResp, callback: (String) => Unit): Unit = {
    if (resp.response.getStatus == -1) {
      val msg = genErrorMessage(s"Request ${resp.response.getUrl} [${resp.response.getContent}]. Please check the backend is alive.")
      ctx.run(query[MlsqlJob].filter(_.name == lift(resp.newparams("jobName"))).
        update(_.status -> lift(MlsqlJob.FAIL),
          _.response -> lift(resp.response.getContent),
          _.finishAt -> lift(System.currentTimeMillis())))
      callback(msg)
    }
  }

  def buildSuccessRecord(resp: RunScriptResp): Unit = {
    if (!resp.isAsync && resp.newparams.contains("jobName")) {
      ctx.run(query[MlsqlJob].filter(_.name == lift(resp.newparams("jobName"))).
        update(_.status -> lift(MlsqlJob.SUCCESS),
          _.response -> lift(resp.response.getContent),
          _.finishAt -> lift(System.currentTimeMillis())))
    }
  }

  def getEngine = {
    val engineName = if (param("engineName") == "undefined" || !hasParam("engineName")) {
      UserService.getBackendName(user).getOrElse("")
    } else param("engineName")
    val engines = user.getEngines
    val engineConfigOpt = engines.filter(_.name == engineName).headOption

    val _proxyUrl = if (clusterUrl != null && !clusterUrl.isEmpty) clusterUrl else engineUrl
    val _myUrl = RunScript.MY_URL
    val _home = s"${MLSQLConsoleCommandConfig.commandConfig.user_home}"
    val _skipAuth = if (!MLSQLConsoleCommandConfig.commandConfig.enable_auth_center) MlsqlEngine.SKIP_AUTH else MlsqlEngine.AUTH

    val engineConfig = engineConfigOpt match {
      case Some(engineConfig) => engineConfig
      case None => EngineService.list().headOption.getOrElse(MlsqlEngine(
        0, "", _proxyUrl, _home, _myUrl, _myUrl, _myUrl, _skipAuth, "{}", ""
      ))
    }
    engineConfig
  }

  def execute(shouldOptimize: Boolean) = {

    val engineConfig = getEngine
    val proxy = RestService.client(engineConfig.url)
    var newparams = params()

    if (!params().contains("jobName")) {
      newparams += ("jobName" -> UUID.randomUUID().toString)
    }

    val quileFileService = new QuillScriptFileService()
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
      case _ =>
        var tempSQL = newparams("sql")
        val hintManager = List(new KylinHint, new PythonHint, new DeployModelHint,new DeployPythonModelHint)
        hintManager.foreach { hinter =>
          if (tempSQL == newparams("sql")) {
            tempSQL = hinter.rewrite(tempSQL, newparams + ("owner" -> user.name) + ("home" -> engineConfig.home))
          }
        }

        if (shouldOptimize && newparams.getOrElse("executeMode", "query") == "query" && newparams.getOrElse("queryType","") != "robot") {
          val optimizer = new IndexOptimizer()
          optimizer.optimize(user, tempSQL)
        } else tempSQL

    }

    val tagsMap = try {
      JSONTool.parseJson[Map[String, String]](user.backendTags)
    } catch {
      case e: Exception => Map(tech.mlsql.model.MlsqlUser.NORMAL_TAG_TYPE -> user.backendTags)
    }


    var tags = if (sql.contains("!scheduler")) {
      tagsMap.get(tech.mlsql.model.MlsqlUser.SCHEDULER_TAG_TYPE).getOrElse("")
    } else {
      tagsMap.get(tech.mlsql.model.MlsqlUser.NORMAL_TAG_TYPE).getOrElse("")
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
    if (!hasParam("callback")) {
      newparams += ("callback" -> s"${engineConfig.consoleUrl}/api_v1/job/callback?__auth_secret__=${RestService.auth_secret}")
    }

    newparams += ("sql" -> sql)

    newparams += ("owner" -> user.name)

    if (!hasParam("schemaInferUrl")) {
      newparams += ("schemaInferUrl" -> (engineConfig.url + "/run/script"))
    }

    def cleanSql(sql: String) = {
      try {
        sql.split("\n").filterNot(line => line.contains("password")).mkString("\n")
      } catch {
        case e: Exception =>
          sql
      }
    }

    logInfo(cleanSql(sql))

    if (hasParam("__connect__")) {
      val connect = MlsqlDs.getConnect(param("__connect__"), user)
      val newSql = connect + sql
      newparams += ("sql" -> newSql)
    }

    val isSaveQuery = newparams.getOrElse("queryType", "human") == "human"

    if (newparams.getOrElse("queryType", "human") == "analysis_workshop_apply_action") {
      newparams += ("timeout" -> (user.apply_timeout * 1000).toString)
    }

    val isAsync = newparams.getOrElse("async", "false").toBoolean
    val startTime = System.currentTimeMillis()

    if (isSaveQuery) {
      ctx.run(query[MlsqlJob].insert(lift(buildJob(user, newparams, MlsqlJob.RUNNING, ""))))
    }

    val time = System.currentTimeMillis()
    val response = proxy.runScript(newparams)
    logInfo(s"proxy response time:${System.currentTimeMillis()-time}")
    RunScriptResp(isSaveQuery, isAsync, startTime, response, newparams, sql: String)
  }

  def buildJob(user: MlsqlUser, newparams: Map[String, String], status: Int, reason: String) = {
    val job = MlsqlJob(0, newparams("jobName"), newparams("sql"), status, user.id, reason, System.currentTimeMillis(), -1, "[]")
    job
  }

}

case class RunScriptResp(isSaveQuery: Boolean, isAsync: Boolean, startTime: Long, response: SResponse, newparams: Map[String, String], sql: String) {
  def buildJob(user: MlsqlUser, status: Int, reason: String) = {
    val job = MlsqlJob(0, newparams("jobName"), sql, status, user.id, reason, System.currentTimeMillis(), -1, "[]")
    job
  }
}

object RunScript {
  val MY_URL = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
    s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
  } else {
    MLSQLConsoleCommandConfig.commandConfig.my_url
  }
}
