package tech.mlsql.api.controller

import java.io.File

import net.csdn.annotation.rest.At
import net.csdn.common.exception.RenderFinish
import net.csdn.modules.http.RestRequest.Method.{GET, POST}
import net.csdn.modules.http.{ApplicationController, AuthModule, RestRequest, ViewType}
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.FileUtils
import tech.mlsql.utils.DownloadRunner

import scala.collection.JavaConversions._

/**
  * 2019-02-17 WilliamZhu(allwefantasy@gmail.com)
  */
class FileServer extends ApplicationController with AuthModule {

  val DEFAULT_TEMP_PATH = "/tmp/upload/"

  @At(path = Array("/api_v1/file/upload"), types = Array(RestRequest.Method.GET, RestRequest.Method.POST))
  def formUpload = {
    tokenAuth(false)
    val sfu = new ServletFileUpload(new DiskFileItemFactory())
    sfu.setHeaderEncoding("UTF-8")
    val items = sfu.parseRequest(request.httpServletRequest())
    try {
      items.filterNot(f => f.isFormField).map {
        item =>
          val fileContent = item.getInputStream()
          val tempFilePath = DEFAULT_TEMP_PATH + md5(user.getName) + s"""${if (item.getFieldName.startsWith("/")) "" else "/"}""" + item.getFieldName
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
        render(500, s"upload fail,check master log ${e.getMessage}")
    }
    throw new RenderFinish()
    //val fields = items.filter(f => f.isFormField)
  }

  @At(path = Array("/api_v1/file/download"), types = Array(GET, POST))
  def download = {
    if (!hasParam("fileName")) {
      render(404, "fileName required")
    }
    try {
      DownloadRunner.getTarFileByPath(restResponse.httpServletResponse(), DEFAULT_TEMP_PATH + md5(param("userName")) + "/" + param("fileName"))
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
}
