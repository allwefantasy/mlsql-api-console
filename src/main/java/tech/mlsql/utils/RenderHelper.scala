package tech.mlsql.utils

import net.csdn.modules.http.ApplicationController

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * 2/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
trait RenderHelper extends ApplicationController {

  def renderWithSchema[T: TypeTag : ClassTag](data: List[T]) = {
    render(200, JSONTool.toJsonStr(Map("schema" -> extractClassName[T], "data" -> data)))
  }

  def extractClassName[T: TypeTag : ClassTag] = {
    //    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    //    val classTest = typeOf[T].typeSymbol.asClass
    val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
    constructor.paramLists.flatten.map((param: Symbol) => param.name.toString)
  }
}
