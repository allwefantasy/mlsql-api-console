package tech.mlsql.utils

import net.liftweb.json
import net.liftweb.json._
import streaming.dsl.auth.OperateType
import streaming.dsl.auth.OperateType.OperateType

/**
  * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
  */
class OperateTypeSerializer
  extends json.Serializer[OperateType] {

  val EnumerationClass = classOf[OperateType]

  def deserialize(implicit format: Formats):
  PartialFunction[(TypeInfo, JValue), OperateType] = {
    case (TypeInfo(EnumerationClass, _), json) => json match {
      case JObject(List(JField(name, JString(value)))) =>
        value match {
          case "load" => OperateType.LOAD
          case "save" => OperateType.SAVE
          case "set" => OperateType.SET
          case "select" => OperateType.SELECT
          case "create" => OperateType.CREATE
          case "drop" => OperateType.DROP
          case "empty" => OperateType.EMPTY
          case "insert" => OperateType.INSERT
          case "directQuery" => OperateType.DIRECT_QUERY
        }


      case value => throw new MappingException("Can't convert " +
        value + " to " + EnumerationClass)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case _ => null
  }
}

