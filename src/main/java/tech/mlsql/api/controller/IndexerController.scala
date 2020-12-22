package tech.mlsql.api.controller

import java.util.UUID

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer._
import tech.mlsql.quill_model.{MlsqlDs, MlsqlIndexer, MlsqlUser}
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

  def isBinlogSupport = {
    // 是否支持binlog
    val jdbcd = MlsqlDs.get(user, param("dbName"), "jdbc").map(item => {
      JSONTool.parseJson[JDBCD](item.params)
    }).head
    try {
      DBInfoUtils.getBinlogInfo(user, jdbcd)
    } catch {
      case e: Exception =>
        render(400, s"数据库 【${jdbcd.name}】要建立实时索引，需要开启binlog支持,row模式")
    }
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
        isBinlogSupport
        val indexer = new MySQLIndexerV2()
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

  @At(path = Array("/api_v1/indexer/all"), types = Array(Method.GET, Method.POST))
  def allIndex = {
    secretAuth
    val tables = JSONTool.parseJson[List[MlsqlOriTable]](param("data"))
    val values = tables.map(item => item.format + "_" + item.path + "_" + item.storageName).toSet
    val tableMapping = tables.map(item => (item.format + "_" + item.path + "_" + item.storageName, item)).toMap
    val temp = ctx.run(ctx.query[MlsqlIndexer].join(ctx.query[MlsqlUser]).on { case (indexer, user) =>
      indexer.mlsqlUserId == user.id
    }.filter { case (item, _) =>
      val value = item.oriFormat + "_" + item.oriPath + "_" + item.oriStorageName
      liftQuery(values).contains(value)
    }.map { case (indexer, user) =>
      MlsqlIndexerWrapper(
        name = indexer.name,
        oriFormat = indexer.oriFormat,
        oriPath = indexer.oriPath,
        oriStorageName = indexer.oriStorageName,
        format = indexer.format,
        path = indexer.path,
        storageName = indexer.storageName,
        status = indexer.status,
        owner = user.name,
        lastStatus = indexer.lastStatus,
        lastFailMsg = indexer.lastFailMsg,
        lastExecuteTime = indexer.lastExecuteTime,
        syncInterval = indexer.syncInterval,
        content = indexer.content,
        indexerConfig = indexer.indexerConfig,
        lastJobId = indexer.lastJobId,
        indexerType = indexer.indexerType
      )

    })
    val res = temp.map { item =>
      val value = item.oriFormat + "_" + item.oriPath + "_" + item.oriStorageName
      (tableMapping(value), item)
    }.toMap
    render(200, JSONTool.toJsonStr(res))
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
