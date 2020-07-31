package tech.mlsql.service

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.quill_model.AppKv

/**
 * 16/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object AppService {
     def appInfo = {
        ctx.run(query[AppKv]).map{item=>
          val value = item.name match {
            case AppKv.CONFIGURED | AppKv.LOGIN |  AppKv.REGISTER |AppKv.CONSOLE=>
              item.value.toBoolean
          }
          (item.name->value)
        }.toMap
     }
}
