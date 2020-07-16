package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.AppKv

/**
 * 15/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class AppController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/app"), types = Array(Method.POST, Method.GET))
  def app = {
    val appInfo = ctx.run(query[AppKv].filter(_.name == lift(AppKv.CONFIGURED))).headOption match {
      case Some(item) => Map(AppKv.CONFIGURED -> item.value.toBoolean)
      case None => Map(AppKv.CONFIGURED -> false)
    }
    render(JSONTool.toJsonStr(appInfo))
  }


}
