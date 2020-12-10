package tech.mlsql.indexer

import tech.mlsql.quill_model.MlsqlUser

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class NoOpIndexer extends BaseIndexer {
  override def run(user: MlsqlUser, jobName: String, engineName: Option[String]): Unit = {}

  override def generate(user: MlsqlUser, params: Map[String, String]): String = {
    ""
  }
}
