package tech.mlsql.indexer

import java.util.UUID

import net.sf.json.JSONObject
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlDs, MlsqlUser}
import tech.mlsql.service.RunScript

import scala.collection.JavaConverters._

/**
 * 12/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object DBInfoUtils {


  def getMinMax(user: MlsqlUser, dbName: String, tableName: String, columnName: String) = {
    val connect = MlsqlDs.getConnect(dbName, user)
    val executor = new RunScript(user, Map(
      "sql" ->
        s"""
           |
           |${connect}
           |run command as JDBC.`mlsql_console._` where
           |`driver-statement-query`="select max(`${columnName}`) as max,min(`${columnName}`) as min from `${tableName}`"
           |and sqlMode="query"
           |as min_max_${dbName}_${tableName}_${columnName};
           |
           |""".stripMargin,
      "includeSchema" -> "true",
      "owner"->"__system__"
    ))
    val resp = executor.execute(false)
    val data = JSONTool.jParseJsonObj(resp.response.getContent).getJSONArray("data").getJSONObject(0)
    val min = data.getLong("min")
    val max = data.getLong("max")
    (min, max)
  }

  def testConnection(user:MlsqlUser,url:String,driver:String,userName:String,password:String) = {
    val uuid = UUID.randomUUID().toString.replaceAll("-","")
    val connect = s"""
       |connect jdbc where
       | url="${url}"
       | and driver="${driver}"
       | and user="${userName}"
       | and password="${password}"
       | as ${uuid};
       |""".stripMargin

    val executor = new RunScript(user, Map(
      "sql" ->
        s"""
           |$connect
           |run command as JDBC.`${uuid}._` where
           |`driver-statement-query`="show tables"
           |and sqlMode="query"
           |as ${uuid}_show_tables;
           |""".stripMargin,
      "includeSchema" -> "true",
      "owner"->"__system__"
    ))
    val resp = executor.execute(false)
    (resp.response.getStatus==200,resp.response.getContent)
  }

  def getTables(user: MlsqlUser, dbName: String) = {
    val connect = MlsqlDs.getConnect(dbName, user)
    val executor = new RunScript(user, Map(
      "sql" ->
        s"""
           |$connect
           |run command as JDBC.`mlsql_console._` where
           |`driver-statement-query`="show tables"
           |and sqlMode="query"
           |as ${dbName}_show_tables;
           |""".stripMargin,
      "includeSchema" -> "true",
      "owner"->"__system__"
    ))
    val resp = executor.execute(false)
    val tables = JSONTool.jParseJsonObj(resp.response.getContent).getJSONArray("data").asScala.map { item =>
      item.asInstanceOf[JSONObject]
    }.map(item => item.asScala.values.head.asInstanceOf[String]).toList
    tables
  }

}
