package tech.mlsql.service

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.quill_model.AppKv

/**
 * 16/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object AppService {
     def appInfo = {
        ctx.run(query[AppKv]).filter{item=>
          List(AppKv.CONFIGURED,AppKv.LOGIN,AppKv.REGISTER).contains(item.name)
        }.map(item=>(item.name->item.value)).toMap
     }
}
