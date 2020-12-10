package tech.mlsql.indexer

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.MlsqlIndexer

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object IndexerUtils {
  def indexers(userId:Int) = {
    ctx.run(ctx.query[MlsqlIndexer].filter(_.mlsqlUserId == lift(userId)).filter(_.lastStatus == lift(MlsqlIndexer.LAST_STATUS_SUCCESS))).map { item =>
      val config = JSONTool.parseJson[MysqlIndexerConfig](item.indexerConfig)
      (config.from, item)
    }.toMap
  }

  def allIndexers = {
    ctx.run(ctx.query[MlsqlIndexer].filter(_.lastStatus == lift(MlsqlIndexer.LAST_STATUS_SUCCESS))).map { item =>
      val config = JSONTool.parseJson[MysqlIndexerConfig](item.indexerConfig)
      (config.from, item)
    }.toMap
  }
}
