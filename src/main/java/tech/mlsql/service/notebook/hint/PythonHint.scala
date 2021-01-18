package tech.mlsql.service.notebook.hint

import java.util.UUID

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class PythonHint extends BaseHint {
  override def rewrite(query: String, options: Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "python") {
      return query
    }
    val input = header.input.getOrElse("command")
    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))
    val cache = header.params.getOrElse("cache", "false").toBoolean
    var cacheStr =
      s"""
         |save overwrite ${output}_0 as parquet.`/tmp/__python__/${output}`;
         |load parquet.`/tmp/__python__/${output}` as ${output};
         |""".stripMargin

    if (!cache) {
      cacheStr = s"select * from ${output}_0 as ${output};"
    }

    s"""
       |!ray on ${input} '''
       |${header.body}
       |''' named ${output}_0;
       |${cacheStr}
       |""".stripMargin

  }
}
