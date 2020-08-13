package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.{MlsqlAnalysisPlugin, ScriptFile, ScriptUserRw}

/**
 * 12/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class AnalysisPluginController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/script_file/plugin/publish"), types = Array(Method.POST))
  def publishAsPlugin = {
    tokenAuth()
    ctx.run(
      ctx.query[ScriptFile].filter(_.id == lift(paramAsInt("id"))).leftJoin(ctx.query[ScriptUserRw]).on { case (sf, sur) => {
        sf.id == sur.scriptFileId
      }
      }.filter(_._2.exists(_.mlsqlUserId==lift(user.id)))
    ).map(_._1).headOption match {
      case Some(s) =>
        val name = if(hasParam("name")) param("name") else s.name
        ctx.run(ctx.query[MlsqlAnalysisPlugin].filter(_.name == lift(name))).headOption match {
          case Some(plugin) =>
            ctx.run(ctx.query[MlsqlAnalysisPlugin].filter(_.id == lift(plugin.id)).update(_.content -> lift(s.content)))
          case None =>
            ctx.run(ctx.query[MlsqlAnalysisPlugin].insert(_.name->lift(name),_.content -> lift(s.content), _.mlsqlUserId -> lift(user.id)))
        }

      case None =>
    }
    render(200, "{}")
  }

  @At(path = Array("/api_v1/script_file/plugins"), types = Array(Method.GET))
  def plugins = {
    tokenAuth()
    render(200, JSONTool.toJsonStr(ctx.run(ctx.query[MlsqlAnalysisPlugin])))
  }

  @At(path = Array("/api_v1/script_file/plugin/get"), types = Array(Method.GET))
  def pluginGet = {
    tokenAuth()
    ctx.run(ctx.query[MlsqlAnalysisPlugin].filter(_.name==lift(param("name")))).headOption match {
      case Some(item)=> render(200,JSONTool.toJsonStr(item))
      case None => render(404,jsonMessage("not found"))
    }
  }

}
