package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.ApplicationController
import net.csdn.modules.http.RestRequest.Method
import net.liftweb.{json => SJSon}
import streaming.dsl.auth.{MLSQLTable, TableType}
import tech.mlsql.model.MlsqlTable
import tech.mlsql.service.{RestService, TableAuthService}
import tech.mlsql.utils.{JSONTool, OperateTypeSerializer}

import scala.collection.JavaConverters._


/**
  * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
  */
class TableAuthController extends ApplicationController {
  @At(path = Array("/api_v1/table/auth"), types = Array(Method.POST))
  def clusterManager = {
    if (!hasParam("auth_secret") || param("auth_secret") != RestService.auth_secret) {
      render(403, "forbidden")
    }
    val tables = parseJson[List[MLSQLTable]](param("tables"))

    //render(200, JSONTool.toJsonStr(tables.map { m => true }.toSeq))

    val home = param("home")
    val authTables = TableAuthService.fetchAuth(param("owner")).asScala.map { f =>
      val operateType = f.getOperateType
      val table = f.attr("mlsqlTable", classOf[MlsqlTable])
      val key = table.getDb + "_" + table.getName + "_" + table.getTableType + "_" + table.getSourceType
      (key, operateType)
    }.toMap

    // now we will check all table's auth
    val finalResult = tables.map { t =>
      (t.db.getOrElse(KEY_WORD) + "_" + t.table.getOrElse(KEY_WORD) + "_" + t.tableType.name + "_" + t.sourceType.getOrElse(KEY_WORD), t.operateType.toString, t)
    }.map { t =>
      checkAuth(t._1, t._3, home, authTables)
    }

    render(200, JSONTool.toJsonStr(finalResult))
  }

  def checkAuth(key: String, t: MLSQLTable, home: String, authTables: Map[String, String]): Boolean = {
    if (forbidden(t, home)) return false
    if (withoutAuthSituation(t, home)) return true
    return authTables.get(key) match {
      case Some(operateType) => operateType == t.operateType.toString
      case None => false
    }

  }


  def withoutAuthSituation(t: MLSQLTable, home: String) = {
    t.tableType.name == TableType.TEMP.name ||
      (t.tableType.name == TableType.HDFS.name && t.table.getOrElse("").startsWith(home)) ||
      (t.tableType.name == "system" && t.table.getOrElse("") != "__resource_allocate__") ||
      t.tableType.name == TableType.GRAMMAR.name || t.operateType.toString == "set"
  }


  def forbidden(t: MLSQLTable, home: String) = {
    (TableType.HDFS.includes.contains(t.db.getOrElse("")) && t.operateType.toString == "select") ||
      t.operateType.toString == "create" ||
      t.operateType.toString == "insert"
  }

  val KEY_WORD = "undefined"

  def parseJson[T](str: String)(implicit m: Manifest[T]) = {
    implicit val formats = SJSon.DefaultFormats + new OperateTypeSerializer()
    SJSon.parse(str).extract[T]
  }
}
