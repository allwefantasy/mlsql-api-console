package tech.mlsql.indexer

import java.util.UUID

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.common.utils.distribute.socket.server.JavaUtils
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlDs, MlsqlIndexer, MlsqlJob, MlsqlUser}
import tech.mlsql.service.RunScript

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class ParquetIndexer extends BaseIndexer {
  override def run(user: MlsqlUser, jobName: String, _engineName: Option[String]): Unit = {
    val indexerInfo = ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(jobName))).head
    // 标记正在执行
    if(indexerInfo.status == MlsqlIndexer.STATUS_INDEXING){
      return
    }
    //相当于上锁
    ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(indexerInfo.id)).update(_.status -> lift(MlsqlIndexer.STATUS_INDEXING)))

    val config = JSONTool.parseJson[Map[String, String]](indexerInfo.indexerConfig)
    val timeout = JavaUtils.timeStringAsMs(config.getOrElse("timeout", "8h"))

    val params = Map(
      "sql" -> indexerInfo.content,
      "owner" -> user.name,
      "async" -> "true",
      "jobName" -> jobName,
      "timeout" -> timeout.toString
    )

    val runScript = new RunScript(user, params)

    val resp = runScript.execute(false)
    // 在历史任务中生成一条记录
    runScript.buildFailRecord(resp, (msg) => {
      //任务提交失败的话 我们要在索引任务里做更新
      ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(indexerInfo.id)).update(
        _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
        _.status -> lift(MlsqlIndexer.STATUS_NONE),
        _.lastFailMsg -> lift(msg)))
    })
    runScript.buildSuccessRecord(resp)

    val startTime = System.currentTimeMillis()
    val monitor = new Thread(new Runnable {
      override def run(): Unit = {
        var jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.id))).head
        var endTime = System.currentTimeMillis()
        while (jobInfo.status == MlsqlJob.RUNNING && (endTime - startTime) < timeout) {
          jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.id))).head
          endTime = System.currentTimeMillis()
          Thread.sleep(5 * 1000)
        }
        val currentTime = System.currentTimeMillis()
        if (jobInfo.status != MlsqlJob.SUCCESS) {

          ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(jobName)).update(
            _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
            _.lastFailMsg -> lift(jobInfo.reason),
            _.status -> lift(MlsqlIndexer.STATUS_NONE),
            _.lastExecuteTime -> lift(currentTime)))

        } else {
          ctx.run(ctx.query[MlsqlIndexer].filter(_.name == lift(jobName)).update(
            _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
            _.lastFailMsg -> "",
            _.status -> lift(MlsqlIndexer.STATUS_NONE),
            _.lastExecuteTime -> lift(currentTime)
          )
          )
        }
      }
    })
    monitor.setDaemon(true)
    monitor.start()
  }

  def paramsToWhere(params: Map[String, String]): String = {
    if (params.size == 0) return ""
    " where " + params.map { case (k, v) =>
      s"`${k}`='''${v}'''"
    }.mkString(" and ")
  }


  override def generate(user: MlsqlUser, params: Map[String, String]): String = {
    val dbName = params("dbName")
    val tableName = params("tableName")
    val uuid = UUID.randomUUID().toString
    val jobName = s"${dbName}.${tableName}-${uuid}"

    val syncIntervalStr = params("syncInterval")
    val syncInterval = JavaUtils.timeStringAsMs(syncIntervalStr)

    //生成脚本
    val format = params("format")
    var newparam = params
    var connect =""

    if (format == "jdbc" && params.get("partitionColumn").isDefined) {
      newparam = newparam ++ Map(
        "load.upperBound" -> params("upperBound"),
        "load.partitionColumn" -> params("partitionColumn"),
        "load.numPartitions" -> params("partitionNumValue"),
        "load.lowerBound" -> params("lowerBound")
      )
    }

    if(format == "jdbc"){
      connect = MlsqlDs.getConnect(dbName, user)
    }

    val tempTableName = uuid.replaceAll("-", "")
    val where = newparam.filter { case (k, v) =>
      k.startsWith("load.")
    }.map { case (k, v) =>
      (k.substring("load.".length), v)
    }.toMap

    val whereStr = paramsToWhere(where)

    val content =
      s"""
         |${connect}
         |load ${format}.`${dbName}.${tableName}` ${whereStr} as ${tempTableName};
         |save overwrite ${tempTableName} as delta.`${format}_${dbName}.${tableName}`;
         |""".stripMargin
    val currentTime = System.currentTimeMillis()
    ctx.run(ctx.query[MlsqlIndexer].insert(
      _.name -> lift(jobName),
      _.syncInterval -> lift(syncInterval),
      _.lastExecuteTime -> lift(currentTime),
      _.status -> lift(MlsqlIndexer.STATUS_NONE),
      _.mlsqlUserId -> lift(user.id),
      _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
      _.indexerConfig -> lift(JSONTool.toJsonStr(newparam+("from"->s"${dbName}.${tableName}"))),
      _.content -> lift(content),
      _.lastFailMsg -> lift(""),
      _.indexerType -> lift(MlsqlIndexer.INDEXER_TYPE_OTHER)
    ))
    jobName
  }
}
