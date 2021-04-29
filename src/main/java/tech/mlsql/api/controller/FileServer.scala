package tech.mlsql.api.controller

import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest.At
import net.csdn.common.collections.WowCollections
import net.csdn.common.exception.RenderFinish
import net.csdn.common.logging.Loggers
import net.csdn.common.network.NetworkUtils
import net.csdn.common.settings.Settings
import net.csdn.modules.http.RestRequest.Method.{GET, POST}
import net.csdn.modules.http.{ApplicationController, AuthModule, RestRequest, ViewType}
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.FileUtils
import tech.mlsql.MLSQLConsoleCommandConfig
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.quill_model.MlsqlEngine
import tech.mlsql.service.{EngineService, RestService, UserService}
import tech.mlsql.utils.DownloadRunner

import scala.collection.JavaConversions._

/**
  * 2019-02-17 WilliamZhu(allwefantasy@gmail.com)
  */
class FileServer extends ApplicationController with AuthModule {

  def getEngine = {
    val clusterUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url
    val engineUrl = MLSQLConsoleCommandConfig.commandConfig.mlsql_engine_url
    val engineName = if(param("engineName")=="undefined" || !hasParam("engineName")){
      UserService.getBackendName(user).getOrElse("")
    }  else param("engineName")

    val engines = user.getEngines
    val engineConfigOpt = engines.filter(_.name==engineName).headOption

    val _proxyUrl = if (clusterUrl != null && !clusterUrl.isEmpty) clusterUrl else engineUrl
    val _myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
      s"http://${NetworkUtils.intranet_ip.getHostAddress}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
    } else {
      MLSQLConsoleCommandConfig.commandConfig.my_url
    }

    val _home = s"${MLSQLConsoleCommandConfig.commandConfig.user_home}"
    val _skipAuth = if (!MLSQLConsoleCommandConfig.commandConfig.enable_auth_center) MlsqlEngine.SKIP_AUTH else MlsqlEngine.AUTH

    val engineConfig = engineConfigOpt match {
      case Some(engineConfig) => engineConfig
      case None => EngineService.list().headOption.getOrElse(MlsqlEngine(
        0, "", _proxyUrl, _home, _myUrl, _myUrl, _myUrl, _skipAuth,"{}",""
      ))
    }
    engineConfig
  }

  @At(path = Array("/api_v1/file/upload"), types = Array(RestRequest.Method.GET, RestRequest.Method.POST))
  def formUpload = {
    tokenAuth(false)
    FileServerDaemon.init
    val sfu = new ServletFileUpload(new DiskFileItemFactory())
    sfu.setHeaderEncoding("UTF-8")
    val items = sfu.parseRequest(request.httpServletRequest())

    val homeDir = new File(FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.name))
    var finalDir = ""
    if (homeDir.exists()) {
      val totalSize = FileUtils.sizeOfDirectory(homeDir)
      if (totalSize > MLSQLConsoleCommandConfig.commandConfig.single_user_upload_bytes.toLong) {
        render(400, s"You have no enough space. The limit is ${MLSQLConsoleCommandConfig.commandConfig.single_user_upload_bytes.toInt} bytes", ViewType.string)
      }
    }

    items.filterNot(f => f.isFormField).headOption match {
      case Some(f) =>
        val prefix = FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.name)
        val itemPath = f.getFieldName
        val chunks = itemPath.split("/").filterNot(f => f.isEmpty)

        if (chunks.filter(f => (f.trim == "." || f.trim == "..")).length != 0) {
          render(400, "file path is not correct")
        }

        if (chunks.size > 0) {
          val file = new File(prefix + "/" + chunks.head)
          FileUtils.deleteQuietly(file)
        } else {
          FileUtils.deleteQuietly(new File(prefix + "/" + itemPath))
        }
      case None =>
    }

    try {
      items.filterNot(f => f.isFormField).map {
        item =>
          val fileContent = item.getInputStream()
          val tempFilePath = PathFun(FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.name)).add(item.getFieldName).toPath
          val dir = new File(tempFilePath.split("/").dropRight(1).mkString("/"))
          if (!dir.exists()) {
            dir.mkdirs()
          }
          val targetPath = new File(tempFilePath)

          if (tempFilePath.substring(homeDir.getPath.length).stripPrefix("/").stripSuffix("/").split("/").length >= 2) {
            finalDir = dir.getPath.substring(homeDir.getPath.length())
          } else {
            finalDir = tempFilePath.substring(homeDir.getPath.length())
          }
          //upload.setSizeMax(yourMaxRequestSize);
          logger.info(s"upload to ${targetPath.getPath}")
          FileUtils.copyInputStreamToFile(fileContent, targetPath)
          fileContent.close()
      }
    } catch {
      case e: Exception =>
        logger.info("upload fail ", e)
        render(500, s"upload fail,check master log ${e.getMessage}", ViewType.string)
    }

    def runUpload() = {

      val engineConfig = getEngine
      val proxy = RestService.client(engineConfig.url)
      var newparams = Map[String, String](
        "sql" ->
          s"""
             |run command as DownloadExt.`` where from="${finalDir}" and to="/tmp/upload";
          """.stripMargin,
        "owner" -> user.name,
        "jobName" -> UUID.randomUUID().toString,
        "sessionPerUser" -> "true",
        "show_stack" -> "false",
        "tags" -> user.backendTags
      )
      
      newparams += ("context.__default__include_fetch_url__" -> s"${engineConfig.consoleUrl}/api_v1/script_file/include")
      newparams += ("context.__default__console_url__" -> s"${engineConfig.consoleUrl}")
      newparams += ("context.__default__fileserver_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/download")
      newparams += ("context.__default__fileserver_upload_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/upload")
      newparams += ("context.__auth_client__" -> s"streaming.dsl.auth.meta.client.MLSQLConsoleClient")
      newparams += ("context.__auth_server_url__" -> s"${engineConfig.authServerUrl}/api_v1/table/auth")
      newparams += ("context.__auth_secret__" -> RestService.auth_secret)
      newparams += ("access_token" -> engineConfig.accessToken)
      newparams += ("defaultPathPrefix" -> PathFun(engineConfig.home).add(user.name).toPath)
      newparams += ("skipAuth" -> (MlsqlEngine.SKIP_AUTH == engineConfig.skipAuth).toString)
      newparams += ("skipGrammarValidate" -> "false")
      
      val response = proxy.runScript(newparams)
      if (response.getStatus != 200) {
        render(500, WowCollections.map("msg", response.getContent), ViewType.json)
      }
    }

    runUpload()

    throw new RenderFinish()
    //val fields = items.filter(f => f.isFormField)
  }

  @At(path = Array("/api_v1/file/download"), types = Array(GET, POST))
  def download = {
    if (!hasParam("auth_secret") || param("auth_secret") != RestService.auth_secret) {
      render(403, "forbidden")
    }

    if (!hasParam("fileName")) {
      render(404, "fileName required")
    }
    var targetFilePath = PathFun(FileServerDaemon.DEFAULT_TEMP_PATH + md5(param("userName"))).add(param("fileName")).toPath
    if (param("fileName").startsWith("public/")) {
      targetFilePath = "/data/mlsql/data/" + param("fileName")
    }
    logger.info(s"Write ${targetFilePath} to response")
    try {
      if (param("fileName").endsWith(".tar")) {
        DownloadRunner.getTarFileByTarFile(restResponse.httpServletResponse(), targetFilePath)
      } else {
        DownloadRunner.getTarFileByPath(restResponse.httpServletResponse(), targetFilePath)
      }

    } catch {
      case e: Exception =>
        logger.error("download fail", e)
    }
    render("", ViewType.stream)
  }

  def md5(text: String): String = {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }


  @At(path = Array("/api_v1/public/file/download"), types = Array(GET, POST))
  def public_download = {
    tokenAuth(false)
    if (!hasParam("fileName")) {
      render(404, "fileName required")
    }


    def runUpload() = {
      val engineConfig = getEngine
      val proxy = RestService.client(engineConfig.url)

      var newparams = Map[String, String](
        "sql" ->
          s"""
             |run command as UploadFileToServerExt.`${param("fileName")}` where tokenName="access-token" and tokenValue="${accessToken}";
          """.stripMargin,
        "owner" -> user.name,
        "jobName" -> UUID.randomUUID().toString,
        "sessionPerUser" -> "true",
        "show_stack" -> "false",
        "tags" -> user.backendTags
      )

      newparams += ("context.__default__include_fetch_url__" -> s"${engineConfig.consoleUrl}/api_v1/script_file/include")
      newparams += ("context.__default__console_url__" -> s"${engineConfig.consoleUrl}")
      newparams += ("context.__default__fileserver_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/download")
      newparams += ("context.__default__fileserver_upload_url__" -> s"${engineConfig.fileServerUrl}/api_v1/file/upload")
      newparams += ("context.__auth_client__" -> s"streaming.dsl.auth.meta.client.MLSQLConsoleClient")
      newparams += ("context.__auth_server_url__" -> s"${engineConfig.authServerUrl}/api_v1/table/auth")
      newparams += ("context.__auth_secret__" -> RestService.auth_secret)
      newparams += ("access_token" -> engineConfig.accessToken)
      newparams += ("defaultPathPrefix" -> PathFun(engineConfig.home).add(user.name).toPath)
      newparams += ("skipAuth" -> (MlsqlEngine.SKIP_AUTH == engineConfig.skipAuth).toString)
      newparams += ("skipGrammarValidate" -> "false")

      val response = proxy.runScript(newparams)
      if (response.getStatus != 200) {
        render(200, WowCollections.map("msg", response.getContent), ViewType.json)
      }
    }

    runUpload()

    val newFile = param("fileName").split("/").filterNot(f => f.isEmpty).last

    val targetFilePath = PathFun(FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.name)).add(newFile).toPath

    try {
      if (newFile.endsWith(".tar")) {
        DownloadRunner.getTarFileByTarFile(restResponse.httpServletResponse(), targetFilePath)
      } else {
        DownloadRunner.getTarFileByPath(restResponse.httpServletResponse(), targetFilePath)
      }

    } catch {
      case e: Exception =>
        logger.error("download fail", e)
    }
    render(200, "", ViewType.stream)
  }

}

object FileServerDaemon {
  val logger = Loggers.getLogger(classOf[FileServer])
  val DEFAULT_TEMP_PATH = "/tmp/upload/"
  val executor = Executors.newSingleThreadScheduledExecutor()
  val uploadTime = new AtomicLong(0)

  def init = {
    if (uploadTime.getAndIncrement() == 0) {
      run
    }
  }

  def run = {
    executor.scheduleWithFixedDelay(new Runnable {
      override def run(): Unit = {
        val file = new File(DEFAULT_TEMP_PATH)
        file.listFiles().foreach { tempFile =>
          try {
            if (System.currentTimeMillis() - tempFile.lastModified() > 1000 * 60 * 120) {
              FileUtils.deleteQuietly(tempFile)
            }
          } catch {
            case e: Exception =>
              logger.error("Delete upload file fail", e)
          }

        }
      }
    }, 60, 60, TimeUnit.SECONDS)
  }
}
