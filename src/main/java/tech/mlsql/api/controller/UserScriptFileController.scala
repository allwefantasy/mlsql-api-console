package tech.mlsql.api.controller

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest._
import net.csdn.jpa.model.Model
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule, ViewType}
import tech.mlsql.model.{MlsqlUser, ScriptFile}
import tech.mlsql.service.{ScriptFileRender, ScriptFileService}
import tech.mlsql.utils.ModelCleaner

@OpenAPIDefinition(
  info = new BasicInfo(
    desc = "Create directory or MLSQL script file.",
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
class UserScriptFileController extends ApplicationController with AuthModule {
  @Action(
    summary = "You can use this API to create a directory or MLSQL script file", description = ""
  )
  @Parameters(Array(
    new Parameter(name = "fileName", required = true, description = "the name of file with suffix", `type` = "string", allowEmptyValue = false),
    new Parameter(name = "isDir", required = true, description = "the file will be created is dir or not", `type` = "boolean", allowEmptyValue = false),
    new Parameter(name = "content", required = false, description = "the content of file will be created", `type` = "string", allowEmptyValue = true)
  ))
  @Responses(Array(
    new ApiResponse(responseCode = "200", description = "", content = new Content(mediaType = "application/json",
      schema = new Schema(`type` = "string", format = """{}""", description = "")
    ))
  ))
  @At(path = Array("/api_v1/script_file"), types = Array(Method.POST))
  def scriptFile = {
    tokenAuth()

    if (hasParam("id")) {
      val sf = ScriptFile.getItem(param("id").toInt)
      if (hasParam("content")) {
        if (user.getStatus == MlsqlUser.STATUS_PAUSE) {
          render(400, s"""{"msg":"you can not operate because this account have be set pause"}""")
        }
        sf.setContent(param("content"))
      }
      if (hasParam("isExpanded")) {
        sf.setIsExpanded(paramAsBoolean("isExpanded", true))
      }
      sf.save()
    } else {
      if (user.getStatus == MlsqlUser.STATUS_PAUSE) {
        render(400, s"""{"msg":"you can not operate because this account have be set pause"}""")
      }
      val parentId = paramAsInt("parentId", -1)
      scriptFileService.createFile(
        user.getName,
        param("fileName"),
        paramAsBoolean("isDir", true),
        param("content"), parentId
      )
    }

    render(200, "{}")
  }

  @At(path = Array("/api_v1/script_file/remove"), types = Array(Method.DELETE, Method.GET, Method.POST))
  def removeScriptFile = {
    tokenAuth()
    if (user.getStatus == MlsqlUser.STATUS_PAUSE) {
      render(400, s"""{"msg":"you can not operate because this account have be set pause"}""")
    }
    scriptFileService.removeFile(paramAsInt("id", -1), user)
    render(200, "{}")
  }

  @Action(
    summary = "You can use this API to get all director/script the user have", description = ""
  )
  @Responses(Array(
    new ApiResponse(responseCode = "200", description = "", content = new Content(mediaType = "application/json",
      schema = new Schema(`type` = "string", format = """{}""", description = "", implementation = classOf[ScriptFileRender])
    ))
  ))
  @At(path = Array("/api_v1/script_file"), types = Array(Method.GET))
  def listScriptFile = {
    tokenAuth()
    var result = scriptFileService.listScriptFileByUser(user)
    if (result == "[]") {
      scriptFileService.createFile(
        user.getName,
        "MLSQL_SCRIPT_CENTOR",
        true,
        null, -1
      )
      Model.commit()
    }
    result = scriptFileService.listScriptFileByUser(user)
    render(200, result)
  }


  @At(path = Array("/api_v1/script_file/get"), types = Array(Method.GET))
  def getScriptFile = {
    tokenAuth()
    val sf = ScriptFile.getItem(paramAsInt("id", -1))
    ModelCleaner.cleanForRender(sf)
    render(200, sf)
  }


  @At(path = Array("/api_v1/script_file/include"), types = Array(Method.GET))
  def includeScriptFile = {
    user = MlsqlUser.findByName(param("owner"))
    val path = param("path")
    val node = scriptFileService.findScriptFileByPath(user, path)
    render(200, node.getContent())
  }

  @At(path = Array("/api_v1/script_file/path/id"), types = Array(Method.GET))
  def pathId = {
    tokenAuth()
    user = MlsqlUser.findByName(param("owner"))
    val path = param("path")
    val node = scriptFileService.findScriptFileByPath(user, path)
    render(200, node.getId,ViewType.string)
  }

  def scriptFileService = ServiceFramwork.injector.getInstance(classOf[ScriptFileService])

}
