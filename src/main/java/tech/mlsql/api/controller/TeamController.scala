package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.quill_model.MlsqlGroup
import tech.mlsql.utils.RenderHelper

/**
 * 2/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class TeamController  extends ApplicationController with AuthModule with RenderHelper{
  @At(path = Array("/api_v1/team"), types = Array(Method.GET))
  def team = {
    tokenAuth()
    val groups = MlsqlGroup.list(user)
    renderWithSchema[MlsqlGroup](groups)
  }

  @At(path = Array("/api_v1/team/create"), types = Array(Method.GET))
  def teamCreate = {
    tokenAuth()
    val groups = MlsqlGroup.list(user)
    renderWithSchema[MlsqlGroup](groups)
  }
}
