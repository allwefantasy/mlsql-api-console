package tech.mlsql.cloud.aliyun

import java.io.File
import java.util.concurrent.Executors

import tech.mlsql.shell.ShellCommand

import scala.collection.mutable.ArrayBuffer

object CreateMLSQLClusterService {
  val executors = Executors.newFixedThreadPool(1)
}

class CreateMLSQLClusterService(ak: String, aks: String, keyPareName: String, scriptLocation: String) {


  def asyncCreate(config: Map[String, String]) = {
    CreateMLSQLClusterService.executors.execute(new Runnable {
      override def run(): Unit = {
        try {
          create(config)
        } catch {
          case e: MLSQLClusterIllegalParameterException =>
          case e: MLSQLClusterCreateFailException =>
          case e: Exception =>
        }

      }
    })
  }

  def create(config: Map[String, String]) = {
    val OSS_AK = System.getenv("OSS_AK")
    val OSS_AKS = System.getenv("OSS_AKS")

    if (OSS_AK == null || OSS_AKS == null) {
      throw new MLSQLClusterIllegalParameterException("OSS_AK/OSS_AKS should be configured when starting MLSQL CONSOLE")
    }

    val env = ArrayBuffer[String]()

    def fetchParam(name: String, require: Boolean) = {
      config.get(name).map { f =>
        env += s"export ${name}=${f}"
      }.getOrElse {
        if (require) {
          throw new MLSQLClusterIllegalParameterException(s"${name} is required")
        }
      }
    }

    fetchParam(CreateMLSQLClusterServiceConfig.SECURITY_GROUP, true)
    fetchParam(CreateMLSQLClusterServiceConfig.MASTER_WITH_PUBLIC_IP, true)

    fetchParam(CreateMLSQLClusterServiceConfig.MLSQL_SLAVE_NUM, false)
    fetchParam(CreateMLSQLClusterServiceConfig.MASTER_INSTANCE_TYPE, false)
    fetchParam(CreateMLSQLClusterServiceConfig.SLAVE_INSTANCE_TYPE, false)
    fetchParam(CreateMLSQLClusterServiceConfig.MLSQL_SPARK_VERSION, false)
    fetchParam(CreateMLSQLClusterServiceConfig.MLSQL_VERSION, false)
    fetchParam(CreateMLSQLClusterServiceConfig.PYMLSQL_VERSIOIN, false)
    fetchParam(CreateMLSQLClusterServiceConfig.MLSQL_THIRD_PARTY_JARS, false)

    if (!new File(keyPareName).exists()) {
      env += s"export MLSQL_INIT_SSH_KEY=true"
    }


    val (status, stdErr, std) = ShellCommand.execWithExitValue(
      s"""
         |cd ${scriptLocation}
         |
         |export AK=${ak}
         |export AKS=${aks}
         |export OSS_AK=${OSS_AK}
         |export OSS_AKS=${OSS_AKS}
         |export ${CreateMLSQLClusterServiceConfig.MLSQL_KEY_PARE_NAME}=${keyPareName}
         |${env.mkString("\n")}
         |
         |./run-mlsql-cluster.sh
       """.stripMargin)

    if (status != 0) {
      throw new MLSQLClusterCreateFailException(stdErr + "\n" + std)
    }
    val msg = stdErr + "\n" + std

    def extractAddr(mark: String) = {
      msg.split("\n").filter(line => line.contains(mark + " http://")).headOption match {
        case Some(line) =>
          line.substring("cluster ui:".length).trim
        case None => throw new MLSQLClusterCreateFailException(stdErr + "\n" + std)
      }
    }

    val clusterAddr = extractAddr("cluster ui:")
    val sparkAddr = extractAddr("spark ui:")
    val mlsqlAddr = extractAddr("mlsql ui/api:")
    ClusterResult(clusterAddr, sparkAddr, mlsqlAddr)
  }
}

class MLSQLClusterCreateFailException(val message: String) extends Exception

class MLSQLClusterIllegalParameterException(val message: String) extends Exception

case class ClusterResult(clusterAddr: String, sparkAddr: String, mlsqlAddr: String)

object CreateMLSQLClusterServiceConfig {
  val SECURITY_GROUP = "SECURITY_GROUP"
  val MLSQL_KEY_PARE_NAME = "MLSQL_KEY_PARE_NAME"
  val MLSQL_SLAVE_NUM = "MLSQL_SLAVE_NUM"

  val MASTER_WITH_PUBLIC_IP = "MASTER_WITH_PUBLIC_IP"
  val MASTER_INSTANCE_TYPE = "MASTER_INSTANCE_TYPE"
  val SLAVE_INSTANCE_TYPE = "SLAVE_INSTANCE_TYPE"
  val MLSQL_SPARK_VERSION = "MLSQL_SPARK_VERSION"
  val MLSQL_VERSION = "MLSQL_VERSION"
  val PYMLSQL_VERSIOIN = "PYMLSQL_VERSIOIN"
  val MLSQL_THIRD_PARTY_JARS = "MLSQL_THIRD_PARTY_JARS"

}

