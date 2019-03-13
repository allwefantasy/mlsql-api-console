package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.model.{MlsqlGroupUser, MlsqlTable, MlsqlUser}
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
    if (!TeamRoleService.checkTeamNameValid(param("name"))) {
      scalaRender(400, Map("msg" -> s"team name ${param("name")} has been taken"))
    }
    val groups = TeamRoleService.teams(user, MlsqlGroupUser.Status.owner).asScala.map(f => Map("name" -> f.getName))
    scalaRender(200, groups)
  }


  @At(path = Array("/api_v1/team/in"), types = Array(Method.POST))
  def teamIn = {
    tokenAuth()
    val groups = TeamRoleService.teamsIn(user).asScala.map(f => Map("name" -> f.getName))
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

  @At(path = Array("/api_v1/team/role/add"), types = Array(Method.POST))
  def teamRoleAdd = {
    tokenAuth()
    val res = TeamRoleService.addRoles(param("teamName"), param("roleNames").split(",").toList.asJava)
    scalaRender(200, Map("msg" -> res))
  }

  @At(path = Array("/api_v1/team/member/accept"), types = Array(Method.POST))
  def accpetTeamMemberAdd = {
    tokenAuth()
    TeamRoleService.updateMemberStatus(user, param("teamName"), MlsqlGroupUser.Status.confirmed)
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/team/member/remove"), types = Array(Method.POST))
  def teamMemberRemove = {
    tokenAuth()
    TeamRoleService.removeMember(param("teamName"), param("userName"))
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
      "status" -> gu.attr("status", classOf[Integer])
    ))
    scalaRender(200, res)
  }

  @At(path = Array("/api_v1/team/roles"), types = Array(Method.POST))
  def teamRoles = {
    tokenAuth()
    val res = TeamRoleService.roles(param("teamName")).asScala.map(gu => Map(
      "name" -> gu.getName
    ))
    scalaRender(200, res)
  }

  @At(path = Array("/api_v1/team/tables"), types = Array(Method.POST))
  def teamTables = {
    tokenAuth()
    val res = TeamRoleService.fetchTables(param("teamName")).asScala.map(item => item.mlsqlTable().fetch().get(0).asInstanceOf[MlsqlTable]).map(table => Map(
      "name" -> table.getName,
      "db" -> table.getDb,
      "tableType" -> table.getTableType,
      "sourceType" -> table.getSourceType,
      "id" -> table.id()
    ))
    scalaRender(200, res)
  }

  @At(path = Array("/api_v1/team/role/remove"), types = Array(Method.POST))
  def teamRoleRemove = {
    tokenAuth()
    TeamRoleService.removeRole(param("teamName"), param("roleName"))
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/team/table/add"), types = Array(Method.POST))
  def teamTableAdd = {
    tokenAuth()
    var newMap = params().asScala.map(f => (f._1, f._2)).toMap - "teamName"
    if (!newMap.contains("sourceType")) {
      newMap += ("sourceType" -> newMap("tableType"))
    }

    TeamRoleService.addTableForTeam(param("teamName"), newMap.asJava)
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/team/table/remove"), types = Array(Method.POST))
  def teamTableRemove = {
    tokenAuth()
    TeamRoleService.removeTable(param("teamName"), paramAsInt("tableId"))
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/role/table/add"), types = Array(Method.POST))
  def RoleTableAdd = {
    tokenAuth()
    TeamRoleService.addTableForRole(param("teamName"),
      param("roleName"),
      paramAsStringArray("tableName", null).toList.map(f => new Integer(f.toInt)).asJava,
      paramAsStringArray("operateType", null).toList.asJava)
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/role/member/add"), types = Array(Method.POST))
  def RoleMemberAdd = {
    tokenAuth()

    if (param("roleName") == "undefined" || param("userName") == "undefined") {
      render(400, Map("msg" -> "roleName userName required"))
    }

    val roleNames = paramAsStringArray("roleName", null).toList
    val userNames = paramAsStringArray("userName", null).toList

    roleNames.foreach { roleName =>
      userNames.foreach { userName =>
        TeamRoleService.addMemberForRole(param("teamName"),
          roleName, userName)
      }
    }

    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/role/members"), types = Array(Method.POST))
  def RoleMemberList = {
    tokenAuth()
    val members = TeamRoleService.roleMembers(param("teamName"), param("roleName"))
    val res = members.asScala.map { item =>
      val user = item.mlsqlUser().fetch().get(0).asInstanceOf[MlsqlUser]
      Map("name" -> user.getName, "id" -> user.getId)
    }
    scalaRender(200, res)
  }

  @At(path = Array("/api_v1/role/member/remove"), types = Array(Method.POST))
  def RoleMemberRemove = {
    tokenAuth()
    TeamRoleService.removeRoleMember(param("teamName"), param("roleName"), param("userName"))
    scalaRender(200, Map("msg" -> "success"))
  }

  @At(path = Array("/api_v1/role/table/remove"), types = Array(Method.POST))
  def RoleTableRemove = {
    tokenAuth()
    TeamRoleService.removeRoleTable(param("teamName"), param("roleName"), paramAsInt("TableId", -1))
    scalaRender(200, Map("msg" -> "success"))
  }


  @At(path = Array("/api_v1/role/tables"), types = Array(Method.POST))
  def roleTables = {
    tokenAuth()
    val res = TeamRoleService.roleTables(param("teamName"),
      param("roleName")).asScala.map { f =>
      val table = f.mlsqlTable().fetch().get(0).asInstanceOf[MlsqlTable]
      RenderTable(table.id(), table.getName, table.getDb, table.getTableType, table.getSourceType,
        f.getOperateType, param("roleName"), param("teamName"))
    }.groupBy(f => f.id).map { f =>
      f._2.head.copy(operateType = f._2.map(tableMap => tableMap.operateType).mkString(","))
    }.toSeq
    scalaRender(200, res)
  }

  @At(path = Array("/api_v1/backends"), types = Array(Method.POST))
  def backends = {
    tokenAuth()
    val res = TeamRoleService.backends(param("teamName")).asScala.map(f => Map("name" -> f.getBackendName))
    scalaRender(200, res)
  }

  def scalaRender(status: Int, obj: AnyRef) = {
    render(status, JSONTool.toJsonStr(obj))
  }

}

case class RenderTable(id: Int, name: String, db: String, tableType: String, sourceType: String,
                       operateType: String, roleName: String, teamName: String)

