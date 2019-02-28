package tech.mlsql.api.controller

import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import net.csdn.ServiceFramwork
import net.csdn.annotation.rest.At
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
import tech.mlsql.service.RestService
import tech.mlsql.utils.DownloadRunner

import scala.collection.JavaConversions._

/**
  * 2019-02-17 WilliamZhu(allwefantasy@gmail.com)
  */
class FileServer extends ApplicationController with AuthModule {


  @At(path = Array("/api_v1/file/upload"), types = Array(RestRequest.Method.GET, RestRequest.Method.POST))
  def formUpload = {
    tokenAuth(false)
    FileServerDaemon.init
    val sfu = new ServletFileUpload(new DiskFileItemFactory())
    sfu.setHeaderEncoding("UTF-8")
    val items = sfu.parseRequest(request.httpServletRequest())

    val homeDir = new File(FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.getName))
    if (homeDir.exists()) {
      val totalSize = FileUtils.sizeOfDirectory(homeDir)
      if (totalSize > MLSQLConsoleCommandConfig.commandConfig.single_user_upload_bytes.toInt) {
        render(400, s"You have no enough space. The limit is ${MLSQLConsoleCommandConfig.commandConfig.single_user_upload_bytes.toInt} bytes", ViewType.string)
      }
    }

    items.filterNot(f => f.isFormField).headOption match {
      case Some(f) =>
        val prefix = FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.getName)
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
          val tempFilePath = FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.getName) + s"""${if (item.getFieldName.startsWith("/")) "" else "/"}""" + item.getFieldName
          val dir = new File(tempFilePath.split("/").dropRight(1).mkString("/"))
          if (!dir.exists()) {
            dir.mkdirs()
          }
          val targetPath = new File(tempFilePath)
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
    throw new RenderFinish()
    //val fields = items.filter(f => f.isFormField)
  }

  @At(path = Array("/api_v1/file/download"), types = Array(GET, POST))
  def download = {
    if (!hasParam("fileName")) {
      render(404, "fileName required")
    }
    var targetFilePath = FileServerDaemon.DEFAULT_TEMP_PATH + md5(param("userName")) + "/" + param("fileName")
    if (param("fileName").startsWith("public/")) {
      targetFilePath = "/data/mlsql/data/" + param("fileName")
    }
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
    render(200, "{}", ViewType.json)
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
      val proxy = RestService.client(MLSQLConsoleCommandConfig.commandConfig.mlsql_cluster_url)
      var newparams = Map[String, String](
        "sql" ->
          s"""
             |run command as UploadFileToServerExt.`${param("fileName")}` where tokenName="access-token" and tokenValue="${accessToken}";
          """.stripMargin,
        "owner" -> user.getName,
        "jobName" -> UUID.randomUUID().toString,
        "sessionPerUser" -> "true",
        "show_stack" -> "false",
        "tags" -> user.getBackendTags
      )
      val myUrl = if (MLSQLConsoleCommandConfig.commandConfig.my_url.isEmpty) {
        s"http://${NetworkUtils.intranet_ip}:${ServiceFramwork.injector.getInstance[Settings](classOf[Settings]).get("http.port")}"
      } else {
        MLSQLConsoleCommandConfig.commandConfig.my_url
      }
      newparams += ("context.__default__include_fetch_url__" -> s"${myUrl}/api_v1/script_file/include")
      newparams += ("context.__default__fileserver_url__" -> s"${myUrl}/api_v1/file/download")
      newparams += ("context.__default__fileserver_upload_url__" -> s"${myUrl}/api_v1/file/upload")
      newparams += ("defaultPathPrefix" -> s"${MLSQLConsoleCommandConfig.commandConfig.user_home}/${user.getName}")
      val response = proxy.runScript(newparams)
    }

    runUpload()

    val targetFilePath = FileServerDaemon.DEFAULT_TEMP_PATH + md5(user.getName) + "/" + param("fileName")

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
            if (System.currentTimeMillis() - tempFile.lastModified() > 1000 * 60 * 5) {
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
