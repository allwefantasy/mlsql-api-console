package tech.mlsql.quill_model

import org.joda.time.DateTime
import tech.mlsql.MlsqlJobRender
import tech.mlsql.common.utils.serder.json.JSONTool

case class ScriptFile(id: Int,
                      name: String,
                      hasCaret: Int,
                      icon: String,
                      label: String,
                      parentId: Int,
                      isDir: Int,
                      content: String,
                      isExpanded: Int
                     ) {
}

case class ScriptUserRw(id: Int,
                        scriptFileId: Int,
                        mlsqlUserId: Int,
                        isOwner: Int,
                        readable: Int,
                        writable: Int,
                        isDelete: Int
                       )

case class MlsqlUser(id: Int,
                     name: String,
                     password: String,
                     backendTags: String,
                     role: String,
                     status: String
                    ) {
  def apply_timeout = {
    try {
      val opts = JSONTool.parseJson[Map[String, String]](backendTags)
      opts("apply_timeout").toInt
    } catch {
      case e: Exception => 10
    }
  }
}

case class AccessToken(id: Int, name: String, mlsqlUserId: Int, createAt: Long)

case class MlsqlJob(id: Int, name: String,
                    content: String,
                    status: Int,
                    mlsqlUserId: Int,
                    reason: String,
                    createdAt: Long,
                    finishAt: Long, response: String) {
  def statusStr = {
    status match {
      case 1 => "running"
      case 2 => "success"
      case 3 => "fail"
      case 4 => "killed"
    }
  }

  def timeFormat = "yyyy-MM-dd HH:mm:SS"

  def createAtStr = new DateTime(createdAt).toString(timeFormat)

  def finishAtStr = new DateTime(finishAt).toString(timeFormat)

  def render = {
    MlsqlJobRender(id, name, content.take(100), statusStr, reason.take(100), createAtStr, finishAtStr, response.take(100))
  }
}

case class AppKv(id: Int, name: String, value: String)

object AppKv {
  val CONFIGURED = "configured"
  val LOGIN = "login"
  val REGISTER = "register"
}

case class MlsqlEngine(id: Int, name: String, url: String, home: String,
                       consoleUrl: String,
                       fileServerUrl: String,
                       authServerUrl: String,
                       skipAuth: Int)

object MlsqlEngine {
  val SKIP_AUTH = 1
  val AUTH = 2
}

case class MlsqlWorkshopTable(id: Int,
                              tableName: String,
                              content: String,
                              mlsqlUserId: Int,
                              sessionId: String,
                              status: Int,
                              tableSchema: String,
                              jobName: String
                             ) {
  def statusStr = {
    status match {
      case 1 => "persisting"
      case 2 => "persisted"
      case 3 => "fail"
      case 4 => "killed"
      case 5 => "un_persisted"
    }
  }
}

object MlsqlWorkshopTable {
  val RUNNING = 1
  val SUCCESS = 2
  val FAIL = 3
  val KILLED = 4
  val VIEW = 5
}


object MlsqlJob {
  val RUNNING = 1
  val SUCCESS = 2
  val FAIL = 3
  val KILLED = 4
}
