package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer.MySQLIndexer
import tech.mlsql.quill_model.MlsqlIndexer
import tech.mlsql.utils.RenderHelper

import scala.collection.JavaConverters._

/**
 * 7/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class IndexerController extends ApplicationController with AuthModule with RenderHelper {
  @At(path = Array("/api_v1/indexer/mysql/build"), types = Array(Method.GET, Method.POST))
  def indexMySQL = {
    tokenAuth(false)
    val reqParams = params().asScala.toMap
    require(reqParams.contains("dbName"), "Table should be selected")
    require(reqParams.contains("idCols"), "idCols is required")
    require(reqParams.contains("indexerType"), "indexerType is required")

    val indexer = new MySQLIndexer()
    val jobName = indexer.generate(user, reqParams)
    indexer.run(user, jobName, reqParams.get("engineName"))
    render(200, JSONTool.toJsonStr(Map("jobName" -> "jobName")))
  }

  @At(path = Array("/api_v1/indexer"), types = Array(Method.GET, Method.POST))
  def indexProgress = {
    tokenAuth(false)
    val temp = ctx.run(ctx.query[MlsqlIndexer].filter(_.mlsqlUserId == lift(user.id)))
    render(200, JSONTool.toJsonStr(temp))
  }

  @At(path = Array("api_v1/indexer/callback"), types = Array(Method.GET, Method.POST))
  def callback = {
    secretAuth
    val eventName = param("eventName")
    val jsonContentStr = param("jsonContent")
    val jsonContent = JSONTool.jParseJsonObj(jsonContentStr)
    val streamName = jsonContent.getString("name")
    val id = jsonContent.getString("id")
    ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(streamName)).update(
      _.lastExecuteTime -> lift(System.currentTimeMillis()),
      _.lastFailMsg -> lift(jsonContentStr),
      _.lastJobId -> lift(id)
    ))
    render(200, "{}")
  }
}
