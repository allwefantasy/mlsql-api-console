package tech.mlsql.test

import net.sf.json.JSONObject
import org.apache.http.client.fluent.{Form, Request}

/**
 * 9/6/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object Test {
  def main(args: Array[String]): Unit = {
//    registerTable(9004)
//    testSuggest(9004)
    'x'.isControl
  }

  def testMLSQL = {
    val time = System.currentTimeMillis()
    val response = Request.Post("http://127.0.0.1:9003/run/script").bodyForm(
      Form.form().
        add("owner", "william").
        add("sql",
          """
            |select 1 as a as b;
            |""".stripMargin).
        add("sessionPerUser", "true").
        add("includeSchema", "true").
        build()
    ).execute().returnContent().asString()
    println(response)
    println(JSONObject.fromObject(response))
  }

  def registerTable(port: Int = 9003) = {
    val time = System.currentTimeMillis()
    val response = Request.Post(s"http://127.0.0.1:${port}/run/script").bodyForm(
      Form.form().add("executeMode", "registerTable").add("schema",
        """
          |CREATE TABLE emps(
          |  empid INT NOT NULL,
          |  deptno INT NOT NULL,
          |  locationid INT NOT NULL,
          |  empname VARCHAR(20) NOT NULL,
          |  salary DECIMAL (18, 2),
          |  PRIMARY KEY (empid),
          |  FOREIGN KEY (deptno) REFERENCES depts(deptno),
          |  FOREIGN KEY (locationid) REFERENCES locations(locationid)
          |);
          |""".stripMargin).add("db", "db1").add("table", "emps").
        add("isDebug", "true").build()
    ).execute().returnContent().asString()
    println(response)
  }

  def testSuggest(port: Int = 9003) = {
    val time = System.currentTimeMillis()
    val response = Request.Post(s"http://127.0.0.1:${port}/run/script").bodyForm(
      Form.form().add("executeMode", "autoSuggest").add("sql",
        """
          |select emp from db1.emps as a;
          |-- load csv.`/tmp.csv` where
          |""".stripMargin).add("lineNum", "2").add("columnNum", "10").
        add("isDebug", "true").build()
    ).execute().returnContent().asString()
    println(response)
  }


}
