package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.AppKv
import tech.mlsql.service.AppService

/**
 * 15/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class AppController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/app"), types = Array(Method.POST, Method.GET))
  def app = {
    val appInfo = AppService.appInfo
    render(JSONTool.toJsonStr(appInfo))
  }


  @At(path = Array("/api_v1/app/save"), types = Array(Method.POST))
  def appSave = {
    tokenAuth()
    require(user.role == "admin", "admin is required")
    val appInfo = AppService.appInfo
    if (hasParam("login")) {
      appInfo.get(AppKv.LOGIN) match {
        case Some(item) =>
          ctx.run(query[AppKv].filter(_.name == lift(AppKv.LOGIN)).update(_.value -> lift(param("login"))))
        case None =>
          ctx.run(query[AppKv].insert(lift(AppKv(0, AppKv.LOGIN, param("login")))))
      }

    }
    if (hasParam("register")) {
      appInfo.get(AppKv.REGISTER) match {
        case Some(item) =>
          ctx.run(query[AppKv].filter(_.name == lift(AppKv.REGISTER)).update(_.value -> lift(param("register"))))
        case None =>
          ctx.run(query[AppKv].insert(lift(AppKv(0, AppKv.REGISTER, param("register")))))
      }

    }
    render(200, "{}")
  }


}
