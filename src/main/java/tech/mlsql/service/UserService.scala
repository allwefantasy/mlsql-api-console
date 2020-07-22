package tech.mlsql.service

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.quill_model.{AccessToken, AppKv, MlsqlUser}

object UserService {

  def findUser(name: String) = {
    ctx.run(query[MlsqlUser].filter(_.name == lift(name))).headOption
  }

  def findUserById(id: Int) = {
    ctx.run(query[MlsqlUser].filter(_.id == lift(id))).headOption
  }

  def systemIsConfigured = {
    val appInfo = AppService.appInfo
    !appInfo.isEmpty && appInfo.getOrElse(AppKv.CONFIGURED, false)
  }

  def logout(tokeName:String) = {
     ctx.run(query[AccessToken].filter(_.name==lift(tokeName)).delete)
  }

  def createUser(name: String, password: String, token: String) = {
    val role = if (!systemIsConfigured) {
      ctx.run(query[AppKv].insert(_.name -> lift(AppKv.CONFIGURED), _.value -> lift("true")))
      USER_ROLE_ADMIN
    } else USER_ROLE_DEVELOPER

    ctx.run(query[MlsqlUser].insert(
      lift(MlsqlUser(0, name, password, "", role, USER_STATUS_NORMAL))
    ))

    val user = findUser(name).head

    ctx.run(query[AccessToken].insert(
      lift(AccessToken(0, token, user.id, System.currentTimeMillis()))
    ))

    user
  }

  def isRegisterEnabled = {
     AppService.appInfo.getOrElse(AppKv.REGISTER,false)
  }

  def isLoginEnabled = {
    AppService.appInfo.getOrElse(AppKv.LOGIN,false)
  }

  def token(tokenName: String) = {
    ctx.run(query[AccessToken].filter(_.name == lift(tokenName))).headOption
  }

  def updatePassword(name:String,password:String) = {
     ctx.run(query[MlsqlUser].filter(_.name == lift(name)).update(_.password-> lift(password)))
     //findUser(name).head
  }

  def findUserByToken(tokenName:String) = {
    ctx.run(query[AccessToken].filter(_.name == lift(tokenName)).leftJoin(query[MlsqlUser]).on{case (token,user)=>
      token.mlsqlUserId == user.id
    }.map{case (_,userOpt) => userOpt}).head
  }

  def login(user:MlsqlUser,token:String) = {
    ctx.run(query[AccessToken].insert(
     lift( AccessToken(0, token, user.id, System.currentTimeMillis()))
    ))
  }

  def users = {
    ctx.run(query[MlsqlUser].map(_.name)).toList
  }

  val USER_STATUS_NORMAL = "normal"; // null or normal means ok
  val USER_STATUS_PAUSE = "pause"; // shutdown all write/update function
  val USER_STATUS_LOCK = "lock"; // disable login

  val USER_ROLE_ADMIN = "admin"
  val USER_ROLE_DEVELOPER = "developer"
}

