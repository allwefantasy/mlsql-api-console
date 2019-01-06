package tech.mlsql.cloud.aliyun

import java.nio.charset.Charset

import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.{DefaultAcsClient, RpcAcsRequest}
import com.aliyuncs.ecs.model.v20140526.{CreateInstanceRequest, DescribeImagesRequest}

import scala.collection.JavaConversions._

class CreateECSService(keyPairName: String) {

  def auth = {
    val ak = System.getenv("AK")
    val aks = System.getenv("AKS")
    val profile = DefaultProfile.getProfile("cn-hangzhou", ak, aks)
    val client = new DefaultAcsClient(profile)
    client
  }


  val client = auth


  def createAfterPayInstance(internet_max_bandwidth_out: Int = 1,
                             image_id: String = "m-bp19ibpdra8vdltxftbc",
                             instance_type: String = "ecs.ic5.large") = {
    val request = new CreateInstanceRequest()
    request.setImageId(image_id)
    request.setInstanceType(instance_type)
    request.setIoOptimized("optimized")
    request.setSystemDiskCategory("cloud_ssd")
    request.setKeyPairName(keyPairName)
    if (internet_max_bandwidth_out > 0)
      request.setInternetMaxBandwidthOut(internet_max_bandwidth_out)
    val response = client.doAction(request)
    new String(response.getHttpContent, Charset.forName("UTF-8"))
  }


}

object CreateECSService {
  def main(args: Array[String]): Unit = {
//    new CreateECSService().images()
  }
}
