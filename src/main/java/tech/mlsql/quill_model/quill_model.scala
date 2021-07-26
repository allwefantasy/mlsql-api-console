package tech.mlsql.quill_model

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import org.joda.time.DateTime
import tech.mlsql.MlsqlJobRender
import tech.mlsql.api.controller.JDBCD
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

  def getEngines = {
    val engines = ctx.run(
      ctx.query[MlsqlGroupUser].filter(_.mlsqlUserId == lift(id)).
        filter(groupUser => liftQuery(List(MlsqlGroupUser.owner, MlsqlGroupUser.confirmed)).contains(groupUser.status)).
        join(ctx.query[MlsqlGroup]).
        on(_.mlsqlGroupId == _.id).
        map(group => group._2).
        join(ctx.query[MlsqlBackendProxy]).on(_.id == _.mlsqlGroupId).map(_._2).
        join(ctx.query[MlsqlEngine]).on(_.backendName == _.name).map(_._2)
    )

    val engines2 = ctx.run(query[MlsqlEngine]).filter { engine =>
      try {
        val config = JSONTool.parseJson[Map[String, String]](engine.extraOpts)
        config.get("public").map(_.toBoolean).getOrElse(false)
      } catch {
        case e: Exception =>
          false
      }

    }
    engines ++ engines2
  }
}

case class MlsqlDs(id: Int, name: String, format: String, params: String, mlsqlUserId: Int)

object MlsqlDs {
  def save(mlsqlDs: MlsqlDs) = {
    ctx.run(query[MlsqlDs].insert(lift(mlsqlDs)).returningGenerated(_.id))
  }

  def delete(user: MlsqlUser, id: Int) = {
    ctx.run(query[MlsqlDs].filter(_.id == lift(id)).filter(_.mlsqlUserId == lift(user.id)).delete)
  }

  def list(user: MlsqlUser) = {
    ctx.run(query[MlsqlDs].filter(_.mlsqlUserId == lift(user.id)))
  }

  def get(user: MlsqlUser, name: String, format: String) = {
    ctx.run(query[MlsqlDs].filter(_.mlsqlUserId == lift(user.id)).filter(_.name == lift(name)).filter(_.format == lift(format)))
  }

  def getConnect(name: String, user: MlsqlUser) = {
    MlsqlDs.get(user, name, "jdbc").map(item => {
      JSONTool.parseJson[JDBCD](item.params)
    }).map(item => {
      s"""
         |connect jdbc where
         | url="${item.url}"
         | and driver="${item.driver}"
         | and user="${item.user}"
         | and password="${item.password}"
         | as ${item.name};
         |""".stripMargin
    }).head
  }
}

case class MlsqlGroup(id: Int, name: String)

object MlsqlGroup {
  def save(name: String, user: MlsqlUser, status: Int): Unit = {
    ctx.transaction {
      val id = ctx.run(query[MlsqlGroup].insert(_.name -> lift(name)).returningGenerated(_.id))
      ctx.run(query[MlsqlGroupUser].insert(_.mlsqlGroupId -> lift(id), _.mlsqlUserId -> lift(user.id), _.status -> lift(status)))
    }
  }

  def list(user: MlsqlUser) = {
    ctx.run(query[MlsqlGroupUser].
      filter(_.mlsqlUserId == lift(user.id)).
      filter(_.status == lift(MlsqlGroupUser.owner)).
      join(query[MlsqlGroup]).on(_.mlsqlGroupId == _.id).map { case (_, groupOpt) =>
      groupOpt
    }
    ).toList
  }

}

case class MlsqlBackendProxy(id: Int, mlsqlGroupId: Int, backendName: String)

object MlsqlBackendProxy {
  def save(group: MlsqlGroup, backend: MlsqlEngine): Unit = {
    ctx.run(ctx.query[MlsqlBackendProxy].insert(_.mlsqlGroupId -> lift(group.id), _.backendName -> lift(backend.name)))
  }
}

case class MlsqlGroupUser(id: Int, mlsqlGroupId: Int, mlsqlUserId: Int, status: Int)

case class MlsqlGroupScriptFile(id: Int, mlsqlGroupId: Int, scriptFileId: Int, status: Int)

object MlsqlGroupUser {
  val invited = 1
  val confirmed = 2
  val refused = 3
  val owner = 4
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
    MlsqlJobRender(id, name
      , Option(content).map(_.take(100)).getOrElse("")
      , statusStr
      , Option(reason).map(_.take(100)).getOrElse("")
      , createAtStr
      , finishAtStr
      , Option(response).map(_.take(100)).getOrElse("")
    )
  }
}

case class MlsqlApply(id: Int, name: String,
                      content: String,
                      status: Int,
                      mlsqlUserId: Int,
                      reason: String,
                      createdAt: Long,
                      finishAt: Long, response: String, applySql: String) {

  def timeFormat = "yyyy-MM-dd HH:mm:SS"

  def createAtStr = new DateTime(createdAt).toString(timeFormat)

  def finishAtStr = new DateTime(finishAt).toString(timeFormat)
}

case class AppKv(id: Int, name: String, value: String)

case class MlsqlIndexer(id: Int,
                        name: String,
                        oriFormat: String,
                        oriPath: String,
                        oriStorageName: String,
                        format: String,
                        path: String,
                        storageName: String,
                        mlsqlUserId: Int,
                        status: Int,
                        lastStatus: Int,
                        lastFailMsg: String,
                        lastExecuteTime: Long,
                        syncInterval: Long,
                        content: String,
                        indexerConfig: String,
                        lastJobId: String,
                        indexerType: String
                       ) {
  def isRealTime = syncInterval == -1

  def isOneTime = syncInterval == 0

  def isRepeat = syncInterval > 0
}

object MlsqlIndexer {
  val STATUS_NONE = 0
  val STATUS_INDEXING = 1

  val LAST_STATUS_SUCCESS = 0
  val LAST_STATUS_FAIL = 1

  val REAL_TIME = -1
  val ONE_TIME = 0

  val INDEXER_TYPE_MYSQL = "mysql"
  val INDEXER_TYPE_OTHER = "other"
  val INDEXER_TYPE_CUBE = "cube"
  val INDEXER_TYPE_MV = "mv"
}

object AppKv {
  val CONFIGURED = "configured"
  val LOGIN = "login"
  val REGISTER = "register"
  val CONSOLE = "console"
}

case class MlsqlEngine(id: Int, name: String, url: String, home: String,
                       consoleUrl: String,
                       fileServerUrl: String,
                       authServerUrl: String,
                       skipAuth: Int, extraOpts: String, accessToken: String)

object MlsqlEngine {
  val SKIP_AUTH = 1
  val AUTH = 2
}

case class MlsqlAnalysisPlugin(id: Int, name: String, content: String, mlsqlUserId: Int)

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
