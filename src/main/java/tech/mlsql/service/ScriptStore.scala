package tech.mlsql.service

import java.nio.charset.Charset

import org.apache.http.client.fluent.{Form, Request}
import tech.mlsql.common.utils.serder.json.JSONTool

object ScriptStore {

  val PLUGIN_STORE_URL = "http://store.mlsql.tech/run"

  private def getPluginInfo(name: String) = {
    val pluginListResponse = Request.Post(PLUGIN_STORE_URL).connectTimeout(60 * 1000)
      .socketTimeout(60 * 60 * 1000).bodyForm(Form.form().
      add("action", "getPlugin").
      add("pluginName", name).
      add("pluginType", "MLSQL_SCRIPT").
      build(),
      Charset.forName("utf-8")).execute().returnContent().asString(Charset.forName("utf-8"))
    JSONTool.parseJson[List[PluginStoreItem]](pluginListResponse)
  }

  private  def getLatestPluginInfo(name: String) = {
    val plugins = getPluginInfo(name)
    plugins.sortBy(_.version).last
  }

  private def getPluginNameAndVersion(name: String): (String, String) = {
    if (name.contains(":")) {
      name.split(":") match {
        case Array(name, version) => (name, version)
      }
    } else {
      (name, getLatestPluginInfo(name).version)
    }
  }

  def listPlugins = {
    val pluginListResponse = Request.Post(PLUGIN_STORE_URL).connectTimeout(60 * 1000)
      .socketTimeout(60 * 60 * 1000).bodyForm(Form.form().
      add("action", "listPlugins").
      build(),
      Charset.forName("utf-8")).execute().returnContent().asString(Charset.forName("utf-8"))
      JSONTool.parseJson[List[PluginStoreItem]](pluginListResponse).filter(_.pluginType == 2)
  }

  def fetchSource(path: String, options: Map[String, String]) = {
    val (name, version) = getPluginNameAndVersion(path)
    val pluginInfoOpt = getPluginInfo(name).filter(item => item.version == version).headOption
    require(pluginInfoOpt.isDefined, s"Plugin ${name}:${version} is not exists")
    val params = JSONTool.parseJson[Map[String, String]](pluginInfoOpt.get.extraParams)
    params
  }
  
}
case class PluginStoreItem(id: Int, name: String, path: String, version: String, pluginType: Int, extraParams: String)