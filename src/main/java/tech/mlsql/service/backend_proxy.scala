package tech.mlsql.service

import java.lang.reflect.Proxy

import com.alibaba.dubbo.rpc.protocol.rest.RestClientProxy
import net.csdn.ServiceFramwork
import net.csdn.annotation.Param
import net.csdn.annotation.rest.At
import net.csdn.common.settings.Settings
import net.csdn.modules.http.RestRequest.Method.{GET, POST}
import net.csdn.modules.threadpool.DefaultThreadPoolService
import net.csdn.modules.transport.{DefaultHttpTransportService, HttpTransportService}
import tech.mlsql.MLSQLConsoleCommandConfig

object CloudProxyBackendService {
  private final val settings: Settings = ServiceFramwork.injector.getInstance(classOf[Settings])
  private final val transportService: HttpTransportService = new DefaultHttpTransportService(new DefaultThreadPoolService(settings), settings)

  def client(): CloudProxyBackendService = BackendRestClient.buildClient[CloudProxyBackendService](MLSQLConsoleCommandConfig.commandConfig.mlsql_cloud_proxy, transportService)

  private def buildClient[T](url: String, transportService: HttpTransportService)(implicit manifest: Manifest[T]): T = {
    val restClientProxy = new RestClientProxy(transportService)
    if (url.startsWith("http:")) {
      restClientProxy.target(url)
    } else {
      restClientProxy.target("http://" + url + "/")
    }
    val clazz = manifest.runtimeClass
    Proxy.newProxyInstance(clazz.getClassLoader, Array(clazz), restClientProxy).asInstanceOf[T]
  }
}

trait CloudProxyBackendService {
  @At(path = Array("/api/create_engine"), types = Array(POST))
  def createEngine(params: Map[String, String]): HttpTransportService.SResponse

  @At(path = Array("/api/delete_engine"), types = Array(POST))
  def deleteEngine(params: Map[String, String]): HttpTransportService.SResponse

  @At(path = Array("/api/status"), types = Array(GET))
  def status(params: Map[String, String]): HttpTransportService.SResponse

  @At(path = Array("/api/list"), types = Array(GET))
  def list(params: Map[String, String]): HttpTransportService.SResponse
}


case class CloudAccessKey(
                           ID: Int,
                           Name: String,
                           AccessKeyID: String,
                           AccessKeySecret: String,

                           K8sAddress: String,
                           DriverCoreNum: Int,
                           DriverMemory: String,
                           ExecutorCoreNum: Int,
                           ExecutorMemory: String,
                           ExecutorNum: Int,
                           ClusterName: String,
                           EngineVersion: String,
                           JarEngineVersion: String,
                           OSSBucket: String,
                           ReginID: String,
                           EndpointPublicAccess: Boolean,
                           AccessToken: String
                         )
