package tech.mlsql.indexer

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.sf.json.{JSONArray, JSONObject}
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlIndexer, MlsqlUser}
import tech.mlsql.service.RunScript

import scala.collection.JavaConverters._

/**
 * 8/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class IndexOptimizer extends Logging{
  def optimize(user: MlsqlUser, sql: String): String = {
    val runner = new RunScript(user, Map(
      "sql" -> sql,
      "executeMode" -> "analyze",
      "owner" -> user.name
    ))
    val resp = runner.execute(false)
    val statements = try {
      JSONArray.fromObject(resp.response.getContent)
    } catch {
      case e: Exception =>
        return sql
    }
    val indexers = ctx.run(ctx.query[MlsqlIndexer].filter(_.mlsqlUserId == lift(user.id)).filter(_.lastStatus == lift(MlsqlIndexer.LAST_STATUS_SUCCESS))).map { item =>
      val config = JSONTool.parseJson[MysqlIndexerConfig](item.indexerConfig)
      (config.from, item)
    }.toMap

    val items = statements.asScala.map(item => item.asInstanceOf[JSONObject]).map { item =>
      val raw = item.getString("raw")+";"
      var rewriteRaw = raw
      if (raw.trim.toLowerCase.startsWith("load")) {
        val loadStm = JSONTool.parseJson[LoadStatement](item.toString())
        if (loadStm.format == "jdbc") {
          val cleanPath = cleanStr(loadStm.path)
          val indexer = indexers.get(cleanPath)
          if (indexer.isDefined) {
            var where = ""

            if (!loadStm.option.isEmpty) {
              where = "where " + loadStm.option.map { kv =>
                s"${kv._1} = '''${kv._2}'''"
              }.mkString(" and ")
            }
            rewriteRaw =
              s"""
                 |load delta.`mysql_${cleanPath}` ${where} as ${loadStm.tableName};
                 |""".stripMargin
            logInfo(
              s"""
                 |Rewrite:
                 |${raw}
                 |to
                 |${rewriteRaw}
                 |""".stripMargin)
          }

        }
      }
      rewriteRaw

    }
    items.mkString("\n")
  }
  def cleanStr(str: String) = {
    if (str.startsWith("`") || str.startsWith("\"") || (str.startsWith("'") && !str.startsWith("'''")))
      str.substring(1, str.length - 1)
    else str
  }


  def cleanBlockStr(str: String) = {
    if (str.startsWith("'''") && str.endsWith("'''"))
      str.substring(3, str.length - 3)
    else str
  }
}
