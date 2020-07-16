package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.service.EngineService

/**
 * 16/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class EngineController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/engine/add"), types = Array(Method.POST))
  def addEngine = {
    tokenAuth(false)
    val engine = EngineService.findByName(param("name"))
    if (engine.isDefined) {
      render(404, jsonMessage(s"${param("name")} already been take."))
    }
    EngineService.save(param("name"), param("url"))
    render(200, "{}")
  }

}
