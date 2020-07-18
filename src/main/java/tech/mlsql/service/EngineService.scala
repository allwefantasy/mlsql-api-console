package tech.mlsql.service

import net.csdn.jpa.QuillDB.ctx
import net.csdn.jpa.QuillDB.ctx._
import tech.mlsql.quill_model.MlsqlEngine

object EngineService {
  def findByName(name: String) = {
    ctx.run(query[MlsqlEngine].filter(_.name == lift(name))).headOption
  }

  def save(name: String, url: String) = {
    ctx.run(query[MlsqlEngine].insert(lift(MlsqlEngine(0, name, url))))
  }

  def list() = {
    ctx.run(query[MlsqlEngine]).toList
  }
}
