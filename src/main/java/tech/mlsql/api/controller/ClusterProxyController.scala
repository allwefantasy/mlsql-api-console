package tech.mlsql.api.controller

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest._
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.service.RestService

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
    newparams += ("context.__default__include_fetch_url__" -> s"${myUrl}/api_v1/script_file/include")
    newparams += ("context.__default__fileserver_url__" -> s"${myUrl}/api_v1/file/download")
    newparams += ("context.__default__fileserver_upload_url__" -> s"${myUrl}/api_v1/file/upload")
    newparams += ("defaultPathPrefix" -> s"${MLSQLConsoleCommandConfig.commandConfig.user_home}/${user.getName}")
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
    val response = param("action") match {
      case "/backend/list" => proxy.backendList(newparams)
      case "/backend/add" => proxy.backendAdd(newparams)
      case "/backend/remove" => proxy.backendRemove(newparams)
      case "/backend/tags/update" => proxy.backendTagsUpdate(newparams)
    }

    render(response.getStatus, response.getContent)
  }
}
