package tech.mlsql

import net.csdn.ServiceFramwork
import net.csdn.bootstrap.Application
import tech.mlsql.utils.ParamsUtil

/**
  * 2019-01-03 WilliamZhu(allwefantasy@gmail.com)
  */
object MLSQLConsole {


  def main(args: Array[String]): Unit = {
    val params = new ParamsUtil(args)
    MLSQLConsoleCommandConfig.commandConfig = new MLSQLConsoleCommandConfig(params)
    val applicationYamlName = params.getParam("config", "application.yml")
    ServiceFramwork.applicaionYamlName(applicationYamlName)
    ServiceFramwork.scanService.setLoader(classOf[MLSQLConsoleCommandConfig])
    Application.main(args)

  }
}

class MLSQLConsoleCommandConfig(params: ParamsUtil) {
  def mlsql_cluster_url = {
    params.getParam("mlsql_cluster_url", "http://127.0.0.1:9003")
  }

  def my_url = {
    params.getParam("my_url", "")
  }

  def user_home = {
    params.getParam("user_home", "/home/mlsql")
  }

  def single_user_upload_bytes = {
    params.getParam("single_user_upload_bytes", s"${1024 * 1024 * 1024 * 8}")
  }

  def enable_auth_center = {
    params.getParam("enable_auth_center", s"false").toBoolean
  }
}

object MLSQLConsoleCommandConfig {
  var commandConfig: MLSQLConsoleCommandConfig = null
}
