package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlEngine, MlsqlUser}
import tech.mlsql.service.EngineService
import tech.mlsql.utils.RenderHelper

import scala.collection.JavaConverters._

/**
 * 16/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class EngineController extends ApplicationController with AuthModule with RenderHelper {
  @At(path = Array("/api_v1/engine/add"), types = Array(Method.POST))
  def addEngine = {
    tokenAuth(false)
    if(user.role != "admin"){
      render(400, jsonMessage(s"Only admin is allowed to add engine."))
    }
    require(hasParam("name") && hasParam("url"),"name,url are required")
    val engine = EngineService.findByName(param("name"))
    if (engine.isDefined) {
      EngineService.update(user,engine.get.id,params().asScala.toMap)
    }else {
      EngineService.save(user,param("name"), param("url"),params().asScala.toMap)
    }
    render(200, "{}")
  }

  @At(path = Array("/api_v1/engine/list"), types = Array(Method.POST,Method.GET))
  def list = {
    tokenAuth(false)
    renderWithSchema[MlsqlEngine](EngineService.list())
  }

}
