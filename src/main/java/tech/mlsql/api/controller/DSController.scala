package tech.mlsql.api.controller

import java.sql.{Connection, DriverManager}

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.indexer.IndexerUtils
import tech.mlsql.quill_model.MlsqlDs
import tech.mlsql.utils.RenderHelper

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * 3/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class DSController extends ApplicationController with AuthModule with RenderHelper {
  @At(path = Array("/api_v1/ds/add"), types = Array(Method.POST))
  def addDs = {
    tokenAuth()

    val format = param("format")
    val jType = param("jType")

    var newParams = params().asScala.toMap
    (format, jType) match {
      case ("jdbc", "mysql") =>
        val url = s"""jdbc:mysql://${param("host")}:${param("port")}/${param("db")}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false"""
        val driver = "com.mysql.jdbc.Driver"
        newParams += ("url" -> url)
        newParams += ("driver" -> driver)

        def testConnection = {
          try {
            Class.forName(driver)
            DriverManager.getConnection(url, param("user"), param("password"))
          } catch {
            case e: Exception =>
              render(500, e.getMessage)
          }

        }

        testConnection

      case _ =>
    }
    MlsqlDs.save(
      MlsqlDs(0, param("name"), format, JSONTool.toJsonStr(newParams), user.id))
    render(200, JSONTool.toJsonStr(Map()))
  }

  @At(path = Array("/api_v1/ds/list"), types = Array(Method.POST, Method.GET))
  def list = {
    tokenAuth()
    renderWithSchema(MlsqlDs.list(user))
  }

  @At(path = Array("/api_v1/ds/mysql/connect/get"), types = Array(Method.POST, Method.GET))
  def getMySQLConnect = {
    tokenAuth()
    render(200, JSONTool.toJsonStr(Map("connect" -> MlsqlDs.getConnect(param("name"), user))))
  }

  @At(path = Array("/api_v1/ds/remove"), types = Array(Method.POST, Method.GET))
  def remove = {
    tokenAuth()
    MlsqlDs.delete(user, param("id").toInt)
    renderWithSchema(MlsqlDs.list(user))
  }

  @At(path = Array("/api_v1/ds/mysql/column"), types = Array(Method.POST, Method.GET))
  def getColumn = {
    tokenAuth()
    val columnName = param("columnName")
    val dbName = param("dbName")
    val tableName = param("tableName")

    def showTables(db: JDBCD) = {
      var conn: Connection = null
      var min = 0L
      var max = 0L
      try {
        Class.forName(db.driver)
        conn = DriverManager.getConnection(db.url, db.user, db.password)

        val statement = conn.prepareStatement(s"select max(`${columnName}`) as max,min(`${columnName}`) as min from `${tableName}`")
        val tablesRs = statement.executeQuery()

        while (tablesRs.next()) {
          max = tablesRs.getLong(1)
          min = tablesRs.getLong(2)
        }
        tablesRs.close()
        statement.close()
        conn.close()

      } catch {
        case e: Exception =>
          try {
            if (conn != null) {
              conn.close()
            }
          } catch {
            case e: Exception =>
          }
          //connectTimeout=5000&socketTimeout=30000
          e.printStackTrace()
          render(500, e.getMessage)
      }
      (min, max)
    }

    val columnInfo = MlsqlDs.list(user).filter(_.format == "jdbc").map(item => {
      JSONTool.parseJson[JDBCD](item.params)
    }).filter(_.jType == "mysql").filter(_.name == dbName).map(db => {
      showTables(db)
    }).head

    render(200, JSONTool.toJsonStr(Map("min" -> columnInfo._1, "max" -> columnInfo._2)))

  }

  @At(path = Array("/api_v1/ds/mysql/dbs"), types = Array(Method.POST, Method.GET))
  def getDBs = {
    tokenAuth()
    val indexers = IndexerUtils.allIndexers

    def showTables(db: JDBCD) = {
      var conn: Connection = null
      val tables = ArrayBuffer[DSTable]()
      try {
        Class.forName(db.driver)
        conn = DriverManager.getConnection(db.url, db.user, db.password)

        val statement = conn.prepareStatement("show tables")
        val tablesRs = statement.executeQuery()

        while (tablesRs.next()) {
          val tableName = tablesRs.getString(1)
          val key = s"${db.name}.${tableName}"
          if (indexers.contains(key)) {
            tables += DSTable(tableName, Map("indexer" -> indexers(key).name))
          }else {
            tables += DSTable(tableName, Map())
          }

        }
        tablesRs.close()
        statement.close()
        conn.close()

      } catch {
        case e: Exception =>
          try {
            if (conn != null) {
              conn.close()
            }
          } catch {
            case e: Exception =>
          }
          //connectTimeout=5000&socketTimeout=30000
          e.printStackTrace()
          render(500, e.getMessage)
      }
      tables
    }

    val mysqls = MlsqlDs.list(user).filter(_.format == "jdbc").map(item => {
      JSONTool.parseJson[JDBCD](item.params)
    }).filter(_.jType == "mysql").map(db => {
      DSDB(db.name, db.db, showTables(db).toList)
    })

    renderWithSchema(mysqls)
  }
}

case class DSDB(name: String, db: String, tables: List[DSTable])

case class DSTable(name: String, options: Map[String, String])

case class JDBCD(jType: String,
                 name: String,
                 host: String,
                 port: String,
                 db: String,
                 url: String,
                 driver: String,
                 user: String,
                 password: String)

case class JDBCDsLoadParam(
                            partitionColumn: String,
                            lowerBound: String,
                            upperBound: String,
                            numPartitions: String
                          )
