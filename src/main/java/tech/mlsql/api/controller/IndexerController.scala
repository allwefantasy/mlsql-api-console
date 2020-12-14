package tech.mlsql.api.controller

import java.util.UUID

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer.{IndexerUtils, MySQLIndexer, ParquetIndexer}
import tech.mlsql.quill_model.MlsqlIndexer
import tech.mlsql.service.RunScript
import tech.mlsql.utils.RenderHelper

import scala.collection.JavaConverters._

/**
 * 7/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class IndexerController extends ApplicationController with AuthModule with RenderHelper {

  private def getIndexMapper = {
    IndexerUtils.allIndexers
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
    require(reqParams.contains("indexerType"), "indexerType is required")

    if (isIndexerExists(s"${reqParams("dbName")}.${reqParams("tableName")}")) {
      render(400, "indexer aready exists")
    }

    val jobName = reqParams("indexerType") match {
      case "mysql" =>
        require(reqParams.contains("idCols"), "idCols is required")
        val indexer = new MySQLIndexer()
        val jobName = indexer.generate(user, reqParams)
        indexer.run(user, jobName, reqParams.get("engineName"))
        jobName
      case "parquet" =>
        val reqParams = params().asScala.toMap ++ Map("format" -> "jdbc")
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

  @At(path = Array("/api_v1/indexer/remove"), types = Array(Method.GET, Method.POST))
  def indexRemove = {
    tokenAuth(false)
    val temp = ctx.run(ctx.query[MlsqlIndexer].filter(_.mlsqlUserId == lift(user.id)).filter(_.name == lift(param("name")))).head
    if (temp.syncInterval == MlsqlIndexer.REAL_TIME) {
      val config = JSONTool.parseJson[Map[String, String]](temp.indexerConfig)
      val engineName = config("engineName").toString
      val runscript = new RunScript(user, Map(
        "sql" ->
          s"""
             |!kill ${temp.name};
             |""".stripMargin,
        "owner" -> user.name,
        "jobName" -> UUID.randomUUID().toString,
        "engineName" -> engineName
      ))
      runscript.execute(false)
    }
    ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(temp.id)).delete)
    render(200, JSONTool.toJsonStr(temp))
  }

  @At(path = Array("api_v1/indexer/callback"), types = Array(Method.GET, Method.POST))
  def callback = {
    secretAuth
    val streamName = param("streamName")
    val jsonContentStr = param("jsonContent")
    param("eventName") match {
      case "started" =>
      case "progress" =>

        val jsonContent = JSONTool.jParseJsonObj(jsonContentStr)
        val streamName = jsonContent.getString("name")
        val id = jsonContent.getString("id")
        ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(streamName)).update(
          _.lastExecuteTime -> lift(System.currentTimeMillis()),
          _.lastFailMsg -> lift(jsonContentStr),
          _.lastJobId -> lift(id)
        ))
      case "terminated" =>
        ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(streamName)).update(
          _.lastExecuteTime -> lift(System.currentTimeMillis()),
          _.lastFailMsg -> lift(jsonContentStr),
          _.lastJobId -> lift("")
        ))

    }


    render(200, "{}")
  }
}
