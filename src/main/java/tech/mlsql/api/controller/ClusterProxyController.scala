package tech.mlsql.api.controller

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest._
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.model.{MlsqlBackendProxy, MlsqlUser}
import tech.mlsql.service.RestService
import tech.mlsql.utils.JSONTool

import scala.collection.JavaConverters._


class ClusterProxyController extends ApplicationController with AuthModule {

  @At(path = Array("/api_v1/run/script"), types = Array(Method.POST))
  def runScript = {
    tokenAuth()
    val proxy = RestService.client(MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url)
    var newparams = params().asScala.toMap
    val myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }

    val sql = newparams("sql")

    val tagsMap = try {
      JSONTool.parseJson[Map[String, String]](user.getBackendTags)
    } catch {
      case e: Exception => Map(MlsqlUser.NORMAL_TAG_TYPE -> user.getBackendTags)
    }


    val tags = if (sql.contains("!scheduler")) {
      tagsMap.get(MlsqlUser.SCHEDULER_TAG_TYPE).getOrElse("")
    } else {
      tagsMap.get(MlsqlUser.NORMAL_TAG_TYPE).getOrElse("")
    }

    newparams += ("context.__default__include_fetch_url__" -> s"${myUrl}/api_v1/script_file/include")
    newparams += ("context.__default__console_url__" -> s"${myUrl}")
    newparams += ("context.__default__fileserver_url__" -> s"${myUrl}/api_v1/file/download")
    newparams += ("context.__default__fileserver_upload_url__" -> s"${myUrl}/api_v1/file/upload")
    newparams += ("context.__auth_client__" -> s"streaming.dsl.auth.meta.client.MLSQLConsoleClient")
    newparams += ("context.__auth_server_url__" -> s"${myUrl}/api_v1/table/auth")
    newparams += ("context.__auth_secret__" -> RestService.auth_secret)
    newparams += ("tags" -> tags)
    newparams += ("defaultPathPrefix" -> s"${MLSQLConsoleCommandConfig.commandConfig.user_home}/${user.getName}")
    newparams += ("skipAuth" -> (!MLSQLConsoleCommandConfig.commandConfig.enable_auth_center).toString)
    newparams += ("skipGrammarValidate" -> "false")
    val response = proxy.runScript(newparams)
    render(response.getStatus, response.getContent)
  }

  @At(path = Array("/api_v1/cluster"), types = Array(Method.POST))
  def clusterManager = {
    tokenAuth()
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
