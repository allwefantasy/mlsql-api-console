package tech.mlsql.api.controller

import net.csdn.annotation.rest.At
import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import net.csdn.modules.http.RestRequest.Method
import net.csdn.modules.http.{ApplicationController, AuthModule}
import net.sf.json.JSONObject
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.quill_model.MlsqlJob
import tech.mlsql.service.RestService

/**
 * 3/7/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class JobController extends ApplicationController with AuthModule {
  @At(path = Array("/api_v1/job/callback"), types = Array(Method.POST))
  def jobCallBack = {
    require(param("__auth_secret__") == RestService.auth_secret, "__auth_secret__ is not right")

    val jobName = JSONObject.fromObject(param("jobInfo")).getString("jobName")

    if (param("stat") == "succeeded") {
      ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).
        update(_.status -> lift(MlsqlJob.SUCCESS),
          _.response -> lift(param("res")),
          _.finishAt -> lift(System.currentTimeMillis())))
    } else {
      val msg = param("msg")
      ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).update(
        _.status -> lift(MlsqlJob.FAIL),
        _.reason -> lift(msg),
        _.finishAt -> lift(System.currentTimeMillis())))
    }
    render("{}")
  }

  @At(path = Array("/api_v1/job/list"), types = Array(Method.POST, Method.GET))
  def jobList = {
    tokenAuth()
    val items = ctx.run(query[MlsqlJob].filter(_.mlsqlUserId == lift(user.getId)).sortBy(_.createdAt)(Ord.descNullsLast).take(100))
    render(JSONTool.toJsonStr(items.map(_.render)))
  }

  @At(path = Array("/api_v1/job"), types = Array(Method.POST, Method.GET))
  def job = {
    tokenAuth()
    val jobName = param("jobName")
    ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.getId))).headOption match {
      case Some(item) => render(JSONTool.toJsonStr(item))
      case None => render(404, "{}")
    }

  }

  @At(path = Array("/api_v1/job/kill"), types = Array(Method.POST, Method.GET))
  def jobKill = {
    tokenAuth()
    val jobName = param("jobName")
    ctx.run(query[MlsqlJob].filter(_.name == lift(jobName)).filter(_.mlsqlUserId == lift(user.getId)).
      update(_.status -> lift(MlsqlJob.KILLED), _.finishAt -> lift(System.currentTimeMillis()))
    )
    render("{}")

  }
}
