package tech.mlsql.indexer

import java.util.UUID

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.sf.json.{JSONArray, JSONObject}
import tech.mlsql.api.controller.JDBCD
import tech.mlsql.common.utils.Md5
import tech.mlsql.common.utils.distribute.socket.server.JavaUtils
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlDs, MlsqlIndexer, MlsqlJob, MlsqlUser}
import tech.mlsql.service.{RestService, RunScript}

import scala.collection.JavaConverters._

class MySQLIndexerV2 extends BaseIndexer {

  override def run(user: MlsqlUser, jobName: String, engineName: Option[String]): Unit = {
    val uuid = UUID.randomUUID().toString
    ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(jobName)).update(_.lastJobId -> lift(uuid)))
    val job = ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(jobName))).head

    val List(_, fullSyncScript, incrementSyncScript, syncDBBinlog, instanceName) = JSONTool.parseJson[List[String]](job.content)


    val runScript_1 = new RunScript(user, Map())
    val resp_1 = runScript_1.sql(
      """
        |!delta show tables;
        |""".stripMargin).execute(false)

    val tables = JSONArray.fromObject(resp_1.response.getContent).asScala.map(item => item.asInstanceOf[JSONObject]).map { item =>
      val database = item.getString("database")
      val table = item.getString("table")
      (database, table)
    }.filter(_._1 == "_mlsql_indexer_").map(item => item._2).toSet

    if (!tables.contains(Md5.md5Hash(instanceName))) {
      val runScript = new RunScript(user, Map())
      runScript.sql(syncDBBinlog).jobName(instanceName).async(true)
      if (engineName.isDefined) {
        runScript.engineName(engineName.get)
      }
      val resp = runScript.execute(false)
      var isSubmitSuccess = true
      runScript.buildFailRecord(resp, (msg) => {
        //失败提交失败的话 我们要在索引任务里做更新
        ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(job.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
          _.lastFailMsg -> lift(msg)))
        isSubmitSuccess = false
      })

      if (!isSubmitSuccess) {
        return
      }
    }


    // execute fullSysncScript
    var params = Map(
      "sql" -> fullSyncScript,
      "owner" -> user.name,
      "async" -> "true",
      "jobName" -> uuid
    )

    if (engineName.isDefined) {
      params += ("engineName" -> engineName.get)
    }

    val runScript = new RunScript(user, params)

    val resp = runScript.execute(false)
    // 在历史任务中生成一条记录
    runScript.buildFailRecord(resp, (msg) => {
      //失败提交失败的话 我们要在索引任务里做更新
      ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(job.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
        _.lastFailMsg -> lift(msg)))
    })
    runScript.buildSuccessRecord(resp)

    //因为是异步，所以我们要监控下任务是不是最终成功，如果成功，执行第二步
    //硬编码等待第一个任务一个小时，因为可能异步通知有问题，导致这边没有接收到
    val startTime = System.currentTimeMillis()
    val monitor = new Thread(new Runnable {
      override def run(): Unit = {
        var jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(uuid)).filter(_.mlsqlUserId == lift(user.id))).head
        var endTime = System.currentTimeMillis()
        while (jobInfo.status == MlsqlJob.RUNNING && (endTime - startTime) < 60 * 60 * 1000) {
          jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(uuid)).filter(_.mlsqlUserId == lift(user.id))).head
          endTime = System.currentTimeMillis()
          Thread.sleep(5 * 1000)
        }
        if (jobInfo.status != MlsqlJob.SUCCESS) {
          ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(job.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
            _.lastFailMsg -> lift(jobInfo.reason)))

        } else {
          //启动一个流式任务

          var params = Map(
            "sql" -> incrementSyncScript,
            "owner" -> user.name,
            "async" -> "false",
            "jobName" -> jobName)

          val temp = new RunScript(user, params)
          val newIncrementSyncScript = incrementSyncScript.
            replaceAll("__CONSOLE_URL__", temp.getEngine.consoleUrl).
            replaceAll("__AUTH_SECRET__", RestService.auth_secret)
          params += ("sql" -> newIncrementSyncScript)

          val runScript = new RunScript(user, params)
          val resp = runScript.execute(false)
          // 在历史任务中生成一条记录
          runScript.buildFailRecord(resp, (msg) => {
            //失败提交失败的话 我们要在索引任务里做更新
            ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(job.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
              _.lastFailMsg -> lift(msg)))
          })
          runScript.buildSuccessRecord(resp)
          //任务正常运行的话，就可以更新任务状态了
          ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(job.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
            _.lastFailMsg -> lift("")))

        }
      }
    })
    monitor.setDaemon(true)
    monitor.start()

  }


  override def generate(user: MlsqlUser, params: Map[String, String]): String = {
    val uuid = UUID.randomUUID().toString
    val pb = PartitionBean(
      upperBound = params("upperBound").toLong,
      partitionNumValue = params("partitionNumValue").toLong,
      lowerBound = params("lowerBound").toLong,
      dbName = params("dbName"),
      tableName = params("tableName"),
      indexerType = params("indexerType"),
      idCols = params("idCols"),
      partitionColumn = params("partitionColumn"),
      engineName = params("engineName")
    )

    val db: String = pb.dbName
    val tableName: String = pb.tableName
    val partitionColumn: String = pb.partitionColumn
    val partitionNum: Long = pb.partitionNumValue
    val syncInterval = JavaUtils.timeStringAsSec(params.getOrElse("syncInterval", "60s")).toInt

    val jdbcd = MlsqlDs.get(user, db, "jdbc").map(item => {
      JSONTool.parseJson[JDBCD](item.params)
    }).head

    //    val jdbcd = parseUrl(jdbcUrl).copy(name = db, driver = jdbcDriver, user = jdbcUser, password = jdbcPassword)
    val (min, max) = DBInfoUtils.getMinMax(user, db, tableName, partitionColumn)
    val tempName = s"${db}_${tableName}"
    val jobName = s"${db}.${tableName}-${uuid}"
    val connect = MlsqlDs.getConnect(db, user)

    val fullSyncScript =
      s"""
         |${connect}
         |load jdbc.`${db}.${tableName}` where
         |driver="${jdbcd.driver}"
         |and partitionColumn = "${partitionColumn}"
         |and lowerBound = "${min}"
         |and upperBound = "${max}"
         |and numPartitions = "${partitionNum}"
         |as ${tempName};
         |
         |run ${tempName} as TableRepartition.``
         |where partitionNum="${partitionNum}"
         |and partitionType="range"
         |and partitionCols="${partitionColumn}"
         |as ${tempName}_1;
         |
         |save overwrite ${tempName}_1 as delta.`_mlsql_indexer_.mysql_${db}_${tableName}` ;
         |""".stripMargin

    val (file, position) = DBInfoUtils.getBinlogInfo(user, jdbcd)
    val Array(prefix, binlogIndex) = file.split("\\.")

    // the first time

    val instanceName = s"${jdbcd.host}:${jdbcd.port}"
    val tempName2 = Md5.md5Hash(tempName)
    val syncDBBinlog =
      s"""
         |set streamName="${instanceName}";
         |
         |load binlog.`` where
         |host="${jdbcd.host}"
         |and port="${jdbcd.port}"
         |and userName="${jdbcd.user}"
         |and password="${jdbcd.password}"
         |and databaseNamePattern=".+"
         |and tableNamePattern=".+"
         |and bingLogNamePrefix="${prefix}"
         |and binlogIndex="${binlogIndex}"
         |and binlogFileOffset="${position}"
         |as ${tempName2};
         |
         |
         |save append ${tempName2}
         |as rate.`_mlsql_indexer_.binlog_${tempName2}`
         |options mode="Append"
         |and duration="5"
         |and checkpointLocation="/tmp/${tempName2}";
         |""".stripMargin

    val incrementSyncScript =
      s"""
         |set streamName="${jobName}";
         |
         |register ScriptUDF.`` as extractDBAndTable where
         |lang="scala"
         |and code='''def apply(value:String)={
         |   import net.sf.json.{JSONArray, JSONObject}
         |   val wow = JSONObject.fromObject(value)
         |   wow.getString("databaseName") + "." + wow.getString("tableName")
         |}'''
         |and udfType="udf";
         |
         |load rate.`_mlsql_indexer_.binlog_${tempName2}` where
         |as ${tempName};
         |
         |!callback post "__CONSOLE_URL__/api_v1/indexer/callback?auth_secret=__AUTH_SECRET__" when "started,progress,terminated";
         |select * from ${tempName} where extractDBAndTable(value)="${jdbcd.db}.${tableName}"  as  ${tempName}_1;
         |save append ${tempName}_1
         |as rate.`_mlsql_indexer_.mysql_{db}_{table}`
         |options mode="Append"
         |and idCols="${pb.idCols}"
         |and duration="${syncInterval}"
         |and syncType="binlog"
         |and checkpointLocation="/tmp/${tempName}";
         |""".stripMargin

    ctx.run(ctx.query[MlsqlIndexer].insert(
      _.name -> lift(jobName),
      _.syncInterval -> lift(MlsqlIndexer.REAL_TIME.toLong),
      _.lastExecuteTime -> lift(System.currentTimeMillis()),
      _.status -> lift(MlsqlIndexer.STATUS_NONE),
      _.mlsqlUserId -> lift(user.id),
      _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
      _.indexerConfig -> lift(JSONTool.toJsonStr(Map(
        "name" -> jobName,
        "from" -> s"${pb.dbName}.${pb.tableName}",
        "partitionColumn" -> partitionColumn,
        "partitionNum" -> partitionNum.toString,
        "syncInterval" -> syncInterval.toString,
        "engineName" -> pb.engineName,
        "instanceName" -> instanceName
      ))),
      _.content -> lift(JSONTool.toJsonStr(List(jobName, fullSyncScript, incrementSyncScript, syncDBBinlog, instanceName))),
      _.lastFailMsg -> lift(""),
      _.indexerType -> lift(MlsqlIndexer.INDEXER_TYPE_MYSQL)
    ))

    return jobName
  }


}
