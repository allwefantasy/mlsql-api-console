package tech.mlsql.service.notebook.hint

import java.util.UUID

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class JDBCHint extends BaseHint {
  override def rewrite(query: String, options: Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "direct-jdbc") {
      return query
    }
    val db = header.params("db")
    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    s"""
       |load jdbc.`${db}._` where directQuery='''
       |${header.body}
       |''' as ${output};
       |""".stripMargin

  }
}
