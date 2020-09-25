package tech.mlsql.api.controller

import net.csdn.annotation.rest._
import net.csdn.modules.http.{ApplicationController, AuthModule}
import net.csdn.modules.http.RestRequest.Method
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.service.CloudProxyBackendService

import scala.collection.JavaConverters._


class CloudController  extends ApplicationController with AuthModule with Logging {
  @At(path = Array("/api_v1/proxy/api/create_engine"), types = Array(Method.POST))
  def createEngine = {
    tokenAuth()
    val res = CloudProxyBackendService.client().createEngine(params().asScala.toMap++Map("UserName"->user.name))
    render(res.getStatus,res.getContent)
  }

  @At(path = Array("/api_v1/proxy/api/delete_engine"), types = Array(Method.POST))
  def deleteEngine = {
    tokenAuth()
    val res = CloudProxyBackendService.client().deleteEngine(params().asScala.toMap++Map("UserName"->user.name))
    render(res.getStatus,res.getContent)
  }

  @At(path = Array("/api_v1/proxy/api/status"), types = Array(Method.GET))
  def status = {
    tokenAuth()
    val res = CloudProxyBackendService.client().status(params().asScala.toMap++Map("UserName"->user.name))
    render(res.getStatus,res.getContent)
  }
  @At(path = Array("/api_v1/proxy/api/list"), types = Array(Method.GET))
  def list = {
    tokenAuth()
    val res = CloudProxyBackendService.client().list(params().asScala.toMap++Map("UserName"->user.name))
    render(res.getStatus,res.getContent)
  }
}





