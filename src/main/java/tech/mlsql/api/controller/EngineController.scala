package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlBackendProxy, MlsqlEngine, MlsqlGroup, MlsqlGroupUser}
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
    if (user.role != "admin") {
      render(400, jsonMessage(s"Only admin is allowed to add engine."))
    }
    require(hasParam("name") && hasParam("url"), "name,url are required")
    val engine = EngineService.findByName(param("name"))
    if (engine.isDefined) {
      EngineService.update(user, engine.get.id, params().asScala.toMap)
    } else {
      EngineService.save(user, param("name"), param("url"), params().asScala.toMap ++ Map("extraOpts" -> JSONTool.toJsonStr(Map("public" -> "true"))))
    }
    render(200, "{}")
  }

  @At(path = Array("/api_v1/engine/register"), types = Array(Method.POST))
  def registerEngine = {
    tokenAuth(false)
    require(hasParam("name") && hasParam("url"), "name,url are required")
    val engine = EngineService.findByName(param("name"))
    if (engine.isDefined) {
      EngineService.update(user, engine.get.id, params().asScala.toMap)
    } else {
      EngineService.save(user, param("name"), param("url"), params().asScala.toMap)
      val engine = EngineService.findByName(param("name")).get
      MlsqlGroup.list(user).filter(_.name == "default").headOption match {
        case Some(group) =>
          MlsqlBackendProxy.save(group, engine)
        case None =>
          MlsqlGroup.save("default", user,MlsqlGroupUser.owner)
          val group = MlsqlGroup.list(user).filter(_.name == "default").head
          MlsqlBackendProxy.save(group, engine)
      }
    }
    render(200, "{}")
  }

  @At(path = Array("/api_v1/engine/list"), types = Array(Method.POST, Method.GET))
  def list = {
    tokenAuth(false)
    if (user.role == "admin") {
      renderWithSchema[MlsqlEngine](EngineService.list())
    }
    else {
      renderWithSchema[MlsqlEngine](user.getEngines)
    }
  }

  @At(path = Array("/api_v1/engine/remove"), types = Array(Method.POST, Method.GET))
  def remove = {
    tokenAuth(false)
    if (user.role == "admin") {
      EngineService.remove(user, param("id").toInt)
    }
    renderWithSchema[MlsqlEngine](EngineService.list())
  }


}
