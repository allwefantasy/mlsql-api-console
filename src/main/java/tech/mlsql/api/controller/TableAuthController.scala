package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.ApplicationController
import net.csdn.modules.http.RestRequest.Method
import streaming.dsl.auth.MLSQLTable
import tech.mlsql.utils.JSONTool

/**
  * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
  */
class TableAuthController extends ApplicationController {
  @At(path = Array("/api_v1/table/auth"), types = Array(Method.POST))
  def clusterManager = {
    val tables = JSONTool.parseJson[List[MLSQLTable]](param("tables"))
    render(200, "")
  }
}
