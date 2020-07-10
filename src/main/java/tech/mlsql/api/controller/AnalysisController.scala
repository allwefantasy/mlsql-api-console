package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.MlsqlWorkshopTable

/**
 * 10/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class AnalysisController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/analysis/tables"), types = Array(Method.POST, Method.GET))
  def tables = {
    tokenAuth()
    if (hasParam("sessionId")) {
      val items = ctx.run(query[MlsqlWorkshopTable].filter(_.sessionId == lift(param("sessionId"))))
      render(200, JSONTool.toJsonStr(items))
    }
    val items = ctx.run(query[MlsqlWorkshopTable].filter(_.mlsqlUserId == lift(user.getId)))
    render(200, JSONTool.toJsonStr(items))
  }

  @At(path = Array("/api_v1/analysis/tables/save"), types = Array(Method.POST))
  def save = {
    tokenAuth()
    ctx.run(query[MlsqlWorkshopTable].insert(
      _.mlsqlUserId -> lift(user.getId),
      _.tableName -> lift(param("tableName")),
      _.content -> lift(param("sql")),
      _.sessionId -> lift(param("sessionId"))
    ))
    render(200, JSONTool.toJsonStr(Map()))
  }

}
