package tech.mlsql.api.controller

import java.security.MessageDigest
import java.util.UUID

import net.csdn.annotation.rest._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.model.{AccessToken, MlsqlUser}
import tech.mlsql.utils.JSONTool


@OpenAPIDefinition(
  info = new BasicInfo(
    desc = "User related API e.g. login,register,get current userName.",
    state = State.alpha,
    contact = new Contact(url = "https://github.com/allwefantasy", name = "WilliamZhu", email = "allwefantasy@gmail.com"),
    license = new License(name = "Apache-2.0", url = "")),
  externalDocs = new ExternalDocumentation(description =
    """
      This API is alpha, it means we will add or remove actions/parameters/response from this controller.
      Notice that we use Access-Token to auth visit. Please make sure that  Access-Token is added to http header when request.
    """),
  servers = Array()
)
class UserController extends ApplicationController with AuthModule {

  @Action(
    summary = "Register", description = ""
  )
  @Parameters(Array(
    new Parameter(name = "userName", required = true, description = "string. email", allowEmptyValue = false),
    new Parameter(name = "password", required = true, description = "string. password", allowEmptyValue = false)
  ))
  @Responses(Array(
    new ApiResponse(responseCode = "200", description = "", content = new Content(mediaType = "application/json",
      schema = new Schema(`type` = "string", format = "{}", description = "")
    ))
  ))
  @At(path = Array("/api_v1/user/register"), types = Array(Method.POST))
  def userRegister = {
    tokenAuth(true)
    val token = UUID.randomUUID().toString
    if (MlsqlUser.findByName(param("userName")) == null) {
      user = MlsqlUser.createUser(param("userName"), md5(param("password")), token)
    } else {
      render(400, s"""{"msg":"${param("userName")} have be taken"}""")
    }

    restResponse.httpServletResponse().setHeader(ACCESS_TOKEN_NAME, token)
    AccessToken.loginToken(user, token)

    user.createDefaultTeamAndRole()

    render(200, "{}")
  }

  @Action(
    summary = "get the serName of current login user", description = ""
  )
  @Parameters(Array(
    new Parameter(name = "userName", required = true, description = "string. email", allowEmptyValue = false)
  ))
  @Responses(Array(
    new ApiResponse(responseCode = "200", description = "", content = new Content(mediaType = "application/json",
      schema = new Schema(`type` = "string", format = """{"userName":"allwefantasy@gmail.com"}""", description = "")
    ))
  ))
  @At(path = Array("/api_v1/user/userName"), types = Array(Method.GET, Method.POST))
  def userName = {
    tokenAuth()
    if (user.getStatus == MlsqlUser.STATUS_LOCK) {
      render(400, s"""{"msg":"userName${param("userName")} has been lock"}""")
    }
    render(200, map("userName", user.getName, "backendTags", user.getBackendTags, "role", user.getRole))
  }

  @At(path = Array("/api_v1/users"), types = Array(Method.GET, Method.POST))
  def users = {
    tokenAuth()
    val userNames = MlsqlUser.items("name")
    render(200, userNames)
  }

  @At(path = Array("/api_v1/user/tags/update"), types = Array(Method.GET, Method.POST))
  def userTagsUpdate = {
    tokenAuth()
    if (hasParam("backendTags")) {

      def updateTags(user: MlsqlUser) = {
        val res = if (user.getBackendTags != null) {
          try {
            JSONTool.parseJson[Map[String, String]](user.getBackendTags)
          } catch {
            case e: Exception =>
              Map(MlsqlUser.NORMAL_TAG_TYPE -> user.getBackendTags)
          }

        } else Map[String, String]()

        def appendTag(tags: String) = {
          if (paramAsBoolean("append", false)) {
            tags + "," + param("backendTags")
          } else {
            param("backendTags")
          }
        }

        val tagType = if (paramAsBoolean("isScheduler", false)) MlsqlUser.SCHEDULER_TAG_TYPE else MlsqlUser.NORMAL_TAG_TYPE

        val tempMap = res.get(tagType) match {
          case Some(tags) =>
            Map(tagType -> appendTag(tags))

          case None => Map(tagType -> param("backendTags"))
        }

        val finalMap = res ++ tempMap

        user.setBackendTags(JSONTool.toJsonStr(finalMap))
        user.save()
      }

      updateTags(user)

    }
    render(200, map("userName", user.getName, "backendTags", user.getBackendTags, "role", user.getRole))
  }


  def md5(s: String) = {
    new String(MessageDigest.getInstance("MD5").digest(s.getBytes))
  }


  @Action(
    summary = "login", description = ""
  )
  @Parameters(Array(
    new Parameter(name = "userName", required = true, description = "string. email", allowEmptyValue = false),
    new Parameter(name = "password", required = true, description = "string", allowEmptyValue = false)
  ))
  @Responses(Array(
    new ApiResponse(responseCode = "200", description = "", content = new Content(mediaType = "application/json",
      schema = new Schema(`type` = "string", format = """{}""", description = "")
    )),
    new ApiResponse(responseCode = "400", description = "when userName is not exists or password wrong, return 400",
      content = new Content(mediaType = "application/json",
        schema = new Schema(`type` = "string", format = """{}""", description = "")
      ))
  ))
  @At(path = Array("/api_v1/user/login"), types = Array(Method.POST))
  def userLogin = {
    tokenAuth(true)

    val user = MlsqlUser.findByName(param("userName"))
    if (user == null) {
      render(400, s"""{"msg":"userName${param("userName")} is not exists"}""")
    }

    if (user.getPassword != md5(param("password"))) {
      render(400, s"""{"msg":"password  is not correct"}""")
    }
    if (user.getStatus == MlsqlUser.STATUS_LOCK) {
      render(400, s"""{"msg":"userName${param("userName")} has been lock"}""")
    }
    val token = UUID.randomUUID().toString
    restResponse.httpServletResponse().setHeader(ACCESS_TOKEN_NAME, token)
    AccessToken.loginToken(user, token)

    render(200, "{}")
  }

  @At(path = Array("/api_v1/changepassword"), types = Array(Method.POST))
  def changepassword = {
    tokenAuth(false)

    if (user.getStatus == MlsqlUser.STATUS_LOCK) {
      render(400, s"""{"msg":"userName${user.getName} has been lock"}""")
    }

    if (user.getStatus == MlsqlUser.STATUS_PAUSE) {
      render(400, s"""{"msg":"userName${user.getName} has been paused"}""")
    }


    if (user.getPassword == md5(param("password"))) {
      user.setPassword(md5(param("newPassword")))
      user.save()
    } else {
      render(200, JSONTool.toJsonStr(Map("msg" -> "change password fail")))
    }
    render(200, JSONTool.toJsonStr(Map("msg" -> "success")))
  }

  @At(path = Array("/api_v1/test"), types = Array(Method.POST))
  def test = {
    logger.info(toJson(params()))
    render(200, "{}")
  }

}


