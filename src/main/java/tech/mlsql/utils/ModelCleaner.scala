package tech.mlsql.utils

import javax.persistence.{ManyToOne, OneToMany, OneToOne}
import net.csdn.common.reflect.ReflectHelper
import net.csdn.jpa.model.Model

/**
  * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
  */
object ModelCleaner {
  def cleanForRender(model: Model) = {
    (model.getClass.getDeclaredFields ++ model.getClass.getFields).map { field =>
      field.getAnnotations.filter { anno =>
        anno.annotationType() == classOf[OneToMany] || anno.annotationType() == classOf[ManyToOne] || anno.annotationType() == classOf[OneToOne]
      }.headOption match {
        case Some(i) => ReflectHelper.field(model, field.getName, null)
        case None =>
      }
    }
  }
}
