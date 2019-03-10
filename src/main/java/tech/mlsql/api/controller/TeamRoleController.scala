package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.model.{MlsqlGroupUser, MlsqlUser}
import tech.mlsql.service.TeamRoleService
import tech.mlsql.utils.JSONTool

import scala.collection.JavaConverters._

/**
  * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
  */
class TeamRoleController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/team/create"), types = Array(Method.POST))
  def teamCreate = {
    tokenAuth()
    val res = TeamRoleService.createTeam(user, param("name"))
    if (res == TeamRoleService.ReturnCode.SUCCESS) {
      render(200, map())
    }
    render(map("msg", res))
  }

  @At(path = Array("/api_v1/team/name/check"), types = Array(Method.POST))
  def fieldCheck = {
    tokenAuth()
    val res = TeamRoleService.checkTeamNameValid(param("name"))
    scalaRender(200, Map("msg" -> res))
  }

  @At(path = Array("/api_v1/team"), types = Array(Method.POST))
  def team = {
    tokenAuth()
    val groups = TeamRoleService.teams(user, MlsqlGroupUser.Status.owner).asScala.map(f => Map("name" -> f.getName))
    scalaRender(200, groups)
  }

  @At(path = Array("/api_v1/team/joined"), types = Array(Method.POST))
  def teamJoined = {
    tokenAuth()
    val groups = TeamRoleService.teams(user, MlsqlGroupUser.Status.confirmed).asScala.map(f => Map("name" -> f.getName))
    scalaRender(200, groups)
  }

  @At(path = Array("/api_v1/team/invited"), types = Array(Method.POST))
  def teamInvited = {
    tokenAuth()
    val groups = TeamRoleService.teams(user, MlsqlGroupUser.Status.invited).asScala.map(f => Map("name" -> f.getName))
    scalaRender(200, groups)
  }

  @At(path = Array("/api_v1/team/member/add"), types = Array(Method.POST))
  def teamMemberAdd = {
    tokenAuth()
    val res = TeamRoleService.addMember(param("teamName"), param("userNames").split(",").toList.asJava)
    scalaRender(200, Map("msg" -> res))
  }

  @At(path = Array("/api_v1/team/member/accept"), types = Array(Method.POST))
  def accpetTeamMemberAdd = {
    tokenAuth()
    TeamRoleService.updateMemberStatus(user, param("teamName"), MlsqlGroupUser.Status.confirmed)
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/team/member/refuse"), types = Array(Method.POST))
  def refuseTeamMemberAdd = {
    tokenAuth()
    val res = TeamRoleService.updateMemberStatus(user, param("teamName"), MlsqlGroupUser.Status.refused)
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/team/members"), types = Array(Method.POST))
  def teamMembers = {
    tokenAuth()
    val res = TeamRoleService.members(param("teamName")).asScala.map(gu => Map(
      "name" -> gu.mlsqlUser().fetch().get(0).asInstanceOf[MlsqlUser].getName,
      "status" -> gu.attr("status", classOf[String])
    ))
    scalaRender(200, res)
  }

  def scalaRender(status: Int, obj: AnyRef) = {
    render(status, JSONTool.toJsonStr(obj))
  }

}

