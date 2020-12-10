package tech.mlsql.indexer

import tech.mlsql.quill_model.MlsqlUser

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
trait BaseIndexer {

  def run(user: MlsqlUser, jobName: String, engineName: Option[String]): Unit

  def generate(user: MlsqlUser, params: Map[String, String]): String
}
