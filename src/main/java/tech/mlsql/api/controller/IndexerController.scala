package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer.{MySQLIndexer, MysqlIndexerConfig, ParquetIndexer}
import tech.mlsql.quill_model.MlsqlIndexer
import tech.mlsql.utils.RenderHelper

import scala.collection.JavaConverters._

/**
 * 7/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class IndexerController extends ApplicationController with AuthModule with RenderHelper {

  private def getIndexMapper = {
    ctx.run(ctx.query[MlsqlIndexer].filter(_.mlsqlUserId == lift(user.id)).filter(_.lastStatus == lift(MlsqlIndexer.LAST_STATUS_SUCCESS))).map { item =>
      val config = JSONTool.parseJson[MysqlIndexerConfig](item.indexerConfig)
      (config.from, item)
    }.toMap
  }

  private def isIndexerExists(name: String) = {
    val indexers = getIndexMapper
    indexers.contains(name)
  }

  @At(path = Array("/api_v1/indexer/mysql/build"), types = Array(Method.GET, Method.POST))
  def indexMySQL = {
    tokenAuth(false)
    val reqParams = params().asScala.toMap
    require(reqParams.contains("dbName"), "Table should be selected")
    require(reqParams.contains("idCols"), "idCols is required")
    require(reqParams.contains("indexerType"), "indexerType is required")

    if (isIndexerExists(s"${reqParams("dbName")}.${reqParams("tableName")}")) {
      render(400, "indexer aready exists")
    }

    val jobName = reqParams("indexerType") match {
      case "mysql" =>
        val indexer = new MySQLIndexer()
        val jobName = indexer.generate(user, reqParams)
        indexer.run(user, jobName, reqParams.get("engineName"))
        jobName
      case "parquet" =>
        val reqParams = params().asScala.toMap
        require(reqParams.contains("indexerType"), "indexerType is required")
        val indexer = new ParquetIndexer()
        val jobName = indexer.generate(user, reqParams)
        jobName
    }

    render(200, JSONTool.toJsonStr(Map("jobName" -> jobName)))
  }

  @At(path = Array("/api_v1/indexer/parquet/build"), types = Array(Method.GET, Method.POST))
  def index = {
    tokenAuth(false)

    if (isIndexerExists(s"${param("dbName")}.${param("tableName")}")) {
      render(400, "indexer aready exists")
    }

    val reqParams = params().asScala.toMap
    require(reqParams.contains("indexerType"), "indexerType is required")
    val indexer = new ParquetIndexer()
    val jobName = indexer.generate(user, reqParams)
    render(200, JSONTool.toJsonStr(Map("jobName" -> jobName)))
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
