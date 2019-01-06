package tech.mlsql.service

import com.google.inject.Singleton
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import tech.mlsql.model.{MlsqlUser, ScriptFile}
import tech.mlsql.utils.IDParentID

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

@Singleton
class ScriptFileService {
  def createFile(userName: String, fileName: String, isDir: Boolean, content: String, parentId: Int): Unit = {
    val user = MlsqlUser.findByName(userName)
    ScriptFile.createScriptFile(user, fileName, content, isDir, parentId)
  }

  def removeFile(id: Int, user: MlsqlUser): Unit = {
    ScriptFile.removeScriptFile(id, user)
  }

  def listScriptFileByUser(user: MlsqlUser): String = {
    val res = user.listScriptFiles().map { sur =>
      val sf = sur.scriptFile().fetch().get(0).asInstanceOf[ScriptFile]
      ScriptFileRender(sf.getId, sf.getIcon, sf.getLabel, sf.getParentId, sf.isDir, sf.getIsExpanded)
    }
    implicit val formats = Serialization.formats(NoTypeHints)
    val ser = write(res)
    ser
  }

  def buildTree(user: MlsqlUser) = {
    val items = user.listScriptFiles().map { sur =>
      val sf = sur.scriptFile().fetch().get(0).asInstanceOf[ScriptFile]
      IDParentID(sf.getId, sf.getParentId, sf.getName, ArrayBuffer())

    }
    val ROOTS = ArrayBuffer[IDParentID]()
    val tempMap = scala.collection.mutable.HashMap[Any, Int]()
    val itemsWithIndex = items.zipWithIndex
    itemsWithIndex.foreach { case (item, index) =>
      tempMap(item.id) = index
    }
    itemsWithIndex.foreach { case (item, index) =>

      if (item.parentID != null && item.parentID != 0) {
        items(tempMap(item.parentID)).children += item
      } else {
        ROOTS += item
      }
    }
    ROOTS
  }

  def findScriptFileByPath(user: MlsqlUser, path: String) = {
    val scripts = buildTree(user)
    val ROOT = scripts.head
    var tempChildren = ROOT.children
    var finalNode = ROOT

    def splitPath = {
      var temp = path.split("\\.")
      if (path.endsWith(".mlsql")) {
        temp = path.replaceAll("\\.mlsql$", "").split("\\.")
      }
      temp(temp.length - 1) = temp.last + ".mlsql"
      temp
    }

    var fail = false
    splitPath.zipWithIndex.map { case (node, index) =>
      tempChildren.filter(c => c.name == node).headOption match {
        case Some(n) =>
          finalNode = n
        case None =>
          fail = true
      }
      tempChildren = finalNode.children
    }
    if (!fail) {
      ScriptFile.getItem(finalNode.id.asInstanceOf[Integer])
    } else {
      throw new RuntimeException(s"path ${path} is not found")
    }

  }

}

case class ScriptFileRender(id: Int, icon: String, label: String, parentId: Int, isDir: Boolean, isExpanded: Boolean)
