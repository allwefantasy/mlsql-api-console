package tech.mlsql.service

import net.csdn.ServiceFramwork
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.quill_model.{MlsqlEngine, MlsqlUser}

import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._

object EngineService {
  def findByName(name: String) = {
    ctx.run(query[MlsqlEngine].filter(_.name == lift(name))).headOption
  }

  def convertSkipAuth(skipAuth:String) = {
    if(skipAuth.toBoolean) MlsqlEngine.SKIP_AUTH else MlsqlEngine.AUTH
  }

  def update(user:MlsqlUser,id:Int,option: Map[String, String]) = {
    ctx.run(query[MlsqlEngine].update(lift(MlsqlEngine(id, option("name"), option("url"),
      option("home"),
      option("consoleUrl"),
      option("fileServerUrl"),
      option("authServerUrl"),
      convertSkipAuth(option("skipAuth"))
    )
    )))
  }

  def save(user: MlsqlUser, name: String, url: String, option: Map[String, String]) = {
    val myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }
    val home = s"${MLSQLConsoleCommandConfig.commandConfig.user_home}"
    ctx.run(query[MlsqlEngine].insert(lift(MlsqlEngine(0, name, url,
      option.getOrElse("home", home),
      option.getOrElse("consoleUrl", myUrl),
      option.getOrElse("fileServerUrl", myUrl),
      option.getOrElse("authServerUrl", myUrl),
      convertSkipAuth(option.getOrElse("skipAuth","true"))
    )
    )))
  }

  def list() = {
    ctx.run(query[MlsqlEngine]).toList
  }

  def extractClassName[T: TypeTag : ClassTag] = {
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val classTest = typeOf[T].typeSymbol.asClass
    val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
    constructor.paramLists.flatten.map((param: Symbol) => param.name.toString)
  }
}
