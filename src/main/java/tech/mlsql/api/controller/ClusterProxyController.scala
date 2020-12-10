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
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.model.{MlsqlBackendProxy, MlsqlUser}
import tech.mlsql.quill_model.{MlsqlApply, MlsqlDs, MlsqlEngine, MlsqlJob}
import tech.mlsql.service.{EngineService, QuillScriptFileService, RestService, RunScript, UserService}

import scala.collection.JavaConverters._


class ClusterProxyController extends ApplicationController with AuthModule with Logging {

  val clusterUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url
  val engineUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_engine_url

  @At(path = Array("/api_v1/run/script"), types = Array(Method.POST))
  def runScript = {
    tokenAuth()
    val runscript= new RunScript(user,params().asScala.toMap)
    val resp = runscript.execute(true)
    runscript.buildFailRecord(resp,(msg)=>{
      render(500, msg)
    })
    
    if (resp.newparams.getOrElse("queryType", "human") == "analysis_workshop_apply_action") {
      ctx.run(query[MlsqlApply].insert(
        _.name -> lift(param("analysis_workshop_table_name")),
        _.mlsqlUserId -> lift(user.id),
        _.content -> lift(resp.sql),
        _.response -> lift(resp.response.getContent),
        _.createdAt -> lift(resp.startTime),
        _.finishAt -> lift(System.currentTimeMillis()),
        _.status -> lift(resp.response.getStatus),
        _.applySql -> lift(param("analysis_workshop_sql"))
      ))
    }
    
    runscript.buildSuccessRecord(resp)

    render(resp.response.getStatus, resp.response.getContent)
  }

  private def genErrorMessage(msg: String) = {
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
