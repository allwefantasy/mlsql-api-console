package tech.mlsql.indexer

import java.util.concurrent.{Executors, TimeUnit}

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.quill_model.{MlsqlIndexer, MlsqlUser}

/**
 * 10/12/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object IndexerScheduler {
  private val scheduler = Executors.newScheduledThreadPool(1);

  def start: Unit = {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          val indexers = ctx.run(ctx.query[MlsqlIndexer].filter(_.syncInterval > 0))
          indexers.foreach { item =>
            if (System.currentTimeMillis() - item.lastExecuteTime > item.syncInterval) {
              val indexer = item.indexerType match {
                case MlsqlIndexer.INDEXER_TYPE_MYSQL =>
                  //忽略，因为都是流式计算，无需定时执行
                  new NoOpIndexer()
                case MlsqlIndexer.INDEXER_TYPE_CUBE =>
                  ???
                case MlsqlIndexer.INDEXER_TYPE_MV =>
                  ???
                case _ =>
                  new ParquetIndexer()
              }
              val user = ctx.run(ctx.query[MlsqlUser].filter(_.id == lift(item.mlsqlUserId))).head
              //val params = Map[String, String]()
              //val jobName = indexer.generate(user, params)
              indexer.run(user, item.name, None)
            }
          }
        } catch {
          case e: Exception =>
        }

      }
    }, 60, 5, TimeUnit.SECONDS)
  }
}
