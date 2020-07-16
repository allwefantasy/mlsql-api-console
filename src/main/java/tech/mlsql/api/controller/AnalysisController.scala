package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlJob, MlsqlWorkshopTable}

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
    val items = ctx.run(query[MlsqlWorkshopTable].filter(_.mlsqlUserId == lift(user.id)))
    render(200, JSONTool.toJsonStr(items))
  }

  @At(path = Array("/api_v1/analysis/table/get"), types = Array(Method.POST, Method.GET))
  def tableInfo() = {
    tokenAuth()
    ctx.run(query[MlsqlWorkshopTable].filter(_.tableName == lift(param("tableName")))).headOption match {
      case Some(table) =>
        ctx.run(query[MlsqlJob].filter(_.name == lift(table.jobName)).filter(_.status == lift(MlsqlJob.SUCCESS))).headOption match {
          case Some(_) =>
            ctx.run(query[MlsqlWorkshopTable].filter(_.id==lift(table.id)).update(_.status->lift(MlsqlWorkshopTable.SUCCESS)))
            val newTable =  ctx.run(query[MlsqlWorkshopTable].filter(_.id==lift(table.id))).head
            render(JSONTool.toJsonStr(newTable))
          case None =>
        }
        render(JSONTool.toJsonStr(table))
      case None => render(404, JSONTool.toJsonStr(Map("msg" -> s"""table ${param("tableName")} is not found""")))
    }

  }

  @At(path = Array("/api_v1/analysis/tables/save"), types = Array(Method.POST))
  def save = {
    tokenAuth()
    ctx.run(query[MlsqlWorkshopTable].insert(
      _.mlsqlUserId -> lift(user.id),
      _.tableName -> lift(param("tableName")),
      _.content -> lift(param("sql")),
      _.sessionId -> lift(param("sessionId")),
      _.tableSchema -> lift(param("schema")),
      _.status -> lift(param("status").toInt),
      _.jobName -> lift(param("jobName")
      )
    ))
    render(200, JSONTool.toJsonStr(Map()))
  }

}
