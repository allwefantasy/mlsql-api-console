package tech.mlsql.service

import net.csdn.ServiceFramwork
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.quill_model.{MlsqlEngine, MlsqlUser}


object EngineService {
  def findByName(name: String) = {
    ctx.run(query[MlsqlEngine].filter(_.name == lift(name))).headOption
  }

  def convertSkipAuth(skipAuth: String) = {
    if (skipAuth.toBoolean) MlsqlEngine.SKIP_AUTH else MlsqlEngine.AUTH
  }

  def update(user: MlsqlUser, id: Int, option: Map[String, String]) = {
    ctx.run(query[MlsqlEngine].filter(_.id == lift(id)).update(
      _.name -> lift(option("name")),
      _.url -> lift(option("url")),
      _.home -> lift(option("home")),
      _.consoleUrl -> lift(option("consoleUrl")),
      _.fileServerUrl -> lift(option("fileServerUrl")),
      _.authServerUrl -> lift(option("authServerUrl")),
      _.skipAuth -> lift(convertSkipAuth(option("skipAuth"))),
      _.extraOpts -> lift(option.getOrElse("extraOpts", "{}")),
      _.accessToken -> lift(option("accessToken"))
    ))
  }

  def remove(user: MlsqlUser, id: Int) = {
    ctx.run(query[MlsqlEngine].filter(_.id == lift(id)).delete)
  }

  def save(user: MlsqlUser, name: String, url: String, option: Map[String, String]) = {
    val myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip.getHostAddress}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }
    val home = s"${MLSQLConsoleCommandConfig.commandConfig.user_home}"
    ctx.run(query[MlsqlEngine].insert(lift(MlsqlEngine(0, name, url,
      option.getOrElse("home", home),
      option.getOrElse("consoleUrl", myUrl),
      option.getOrElse("fileServerUrl", myUrl),
      option.getOrElse("authServerUrl", myUrl),
      convertSkipAuth(option.getOrElse("skipAuth", "true")),
      option.getOrElse("extraOpts", "{}"),
      option.getOrElse("accessToken", "")
    )
    )))
  }

  def list() = {
    ctx.run(query[MlsqlEngine]).toList
  }

}
