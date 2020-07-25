package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.{ApplicationController, AuthModule}
import net.csdn.modules.http.RestRequest.Method
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.service.ScriptStore

/**
 * 25/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class PluginController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/plugin/list"), types = Array(Method.POST,Method.GET))
  def list = {
     tokenAuth()
     val items = ScriptStore.listPlugins.groupBy(_.name).map{item=>
       item._2.sortBy(_.version).last
     }
     render(JSONTool.toJsonStr(items))
  }

  @At(path = Array("/api_v1/plugin/get"), types = Array(Method.POST,Method.GET))
  def get = {
    tokenAuth()
    val params = ScriptStore.fetchSource(param("pluginName"),Map())
    render(JSONTool.toJsonStr(params))
  }

}
