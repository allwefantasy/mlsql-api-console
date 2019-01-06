package net.csdn.modules.http

import tech.mlsql.model.AccessToken
import tech.mlsql.model.MlsqlUser

/**
  * 2018-12-02 WilliamZhu(allwefantasy@gmail.com)
  */
trait AuthModule {
  this: ApplicationController =>
  def tokenAuth(ignoreToken: Boolean = false) = {
    restResponse.httpServletResponse().setHeader("Access-Control-Allow-Origin", "*")
    if (!ignoreToken) {
      accessToken = request.header(ACCESS_TOKEN_NAME)
      if (isEmpty(accessToken)) {
        render(401,"""{"msg":"accessToken is invalidate"}""")
      }
      val token = AccessToken.token(accessToken)
      if (isNull(token)) {
        render(401,"""{"msg":"accessToken is invalidate"}""")
      }
      user = token.mlsqlUser().fetch().get(0).asInstanceOf[MlsqlUser]
    }

  }

  var accessToken: String = null
  var user: MlsqlUser = null

  val ACCESS_TOKEN_NAME = "access-token"
}
