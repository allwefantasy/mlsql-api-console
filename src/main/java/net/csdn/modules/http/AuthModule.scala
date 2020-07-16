package net.csdn.modules.http

import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.MlsqlUser
import tech.mlsql.service.{RestService, UserService}

/**
 * 2018-12-02 WilliamZhu(allwefantasy@gmail.com)
 */
trait AuthModule {
  this: ApplicationController =>

  def jsonMessage(msg: String) = {
    JSONTool.toJsonStr(Map("msg" -> msg))
  }

  def tokenAuth(ignoreToken: Boolean = false) = {
    restResponse.httpServletResponse().setHeader("Access-Control-Allow-Origin", "*")
    if (!ignoreToken) {
      accessToken = request.header(ACCESS_TOKEN_NAME)
      if (isEmpty(accessToken)) {
        accessToken = request.cookie(ACCESS_TOKEN_NAME)
      }
      if (isEmpty(accessToken)) {
        render(401, """{"msg":"accessToken is invalidate"}""")
      }

      //to interact with service
      if (accessToken == RestService.auth_secret) {
        user = UserService.findUser(param("owner")).head
      } else {
        val token = UserService.token(accessToken)
        if (token.isEmpty) {
          render(401, """{"msg":"accessToken is invalidate"}""")
        }
        user = UserService.findUserById(token.head.mlsqlUserId).head
      }


    }

  }

  var accessToken: String = null
  var user: MlsqlUser = null

  val ACCESS_TOKEN_NAME = "access-token"
}
