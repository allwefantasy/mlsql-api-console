package tech.mlsql.indexer

import java.util.UUID

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.common.utils.distribute.socket.server.JavaUtils
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlIndexer, MlsqlJob, MlsqlUser}
import tech.mlsql.service.RunScript

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class ParquetIndexer extends BaseIndexer {
  override def run(user: MlsqlUser, jobName: String, _engineName: Option[String]): Unit = {
    val indexerInfo = ctx.run(ctx.query[MlsqlIndexer]).filter(_.name == lift(jobName)).head
    val config = JSONTool.parseJson[Map[String, String]](indexerInfo.indexerConfig)
    val jobTimeout = JavaUtils.timeStringAsSec(config.getOrElse("jobTimeout", "8d"))

    val params = Map(
      "sql" -> indexerInfo.content,
      "owner" -> user.name,
      "async" -> "true",
      "jobName" -> jobName
    )

    val runScript = new RunScript(user, params)

    val resp = runScript.execute(false)
    // 在历史任务中生成一条记录
    runScript.buildFailRecord(resp, (msg) => {
      //失败提交失败的话 我们要在索引任务里做更新
      ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(indexerInfo.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
        _.lastFailMsg -> lift(msg)))
    })
    runScript.buildSuccessRecord(resp)

    val startTime = System.currentTimeMillis()
    val monitor = new Thread(new Runnable {
      override def run(): Unit = {
        var jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.id))).head
        var endTime = System.currentTimeMillis()
        while (jobInfo.status == MlsqlJob.RUNNING && (endTime - startTime) < jobTimeout) {
          jobInfo = ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.id))).head
          endTime = System.currentTimeMillis()
          Thread.sleep(5 * 1000)
        }
        if (jobInfo.status != MlsqlJob.SUCCESS) {
          ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(jobInfo.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_FAIL),
            _.lastFailMsg -> lift(jobInfo.reason)))

        } else {
          ctx.run(ctx.query[MlsqlIndexer].filter(_.id == lift(jobInfo.id)).update(_.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
            _.lastFailMsg -> ""))
        }
      }
    })
    monitor.setDaemon(true)
    monitor.start()
  }

  def paramsToWhere(params: Map[String, String]): String = {
    if (params.size == 0) return ""
    " where " + params.map { case (k, v) =>
      s"`${k}`" = s"''' ${v} '''"
    }.mkString(" and ")
  }


  override def generate(user: MlsqlUser, params: Map[String, String]): String = {
    val dbName = params("dbName")
    val tableName = params("tableName")
    val uuid = UUID.randomUUID().toString
    val jobName = s"${dbName}.${tableName}-${uuid}"

    //生成脚本
    val format = params("format")
    val tempTableName = uuid.replaceAll("-", "")
    val where = params.filter { case (k, v) =>
      k.startsWith("load.")
    }.map { case (k, v) =>
      (k.substring("load.".length), v)
    }.toMap

    val content =
      s"""
         |load ${format}.`${dbName}.${tableName}` ${paramsToWhere(where)} as ${tempTableName};
         |save overwrite ${tempTableName} as delta.`${format}_${dbName}.${tableName}`;
         |""".stripMargin

    ctx.run(ctx.query[MlsqlIndexer].insert(
      _.name -> lift(jobName),
      _.syncInterval -> lift(MlsqlIndexer.REAL_TIME.toLong),
      _.lastExecuteTime -> lift(System.currentTimeMillis()),
      _.status -> lift(MlsqlIndexer.STATUS_NONE),
      _.mlsqlUserId -> lift(user.id),
      _.lastStatus -> lift(MlsqlIndexer.LAST_STATUS_SUCCESS),
      _.indexerConfig -> lift(JSONTool.toJsonStr(params)),
      _.content -> lift(content),
      _.lastFailMsg -> "",
      _.indexerType -> MlsqlIndexer.INDEXER_TYPE_OTHER
    ))
    jobName
  }
}
