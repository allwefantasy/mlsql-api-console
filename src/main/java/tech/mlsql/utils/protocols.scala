package tech.mlsql.utils

/**
  * 2019-01-05 WilliamZhu(allwefantasy@gmail.com)
  */
case class IDParentID(id: Any,
                      parentID: Any,
                      name: String,
                      children: scala.collection.mutable.ArrayBuffer[IDParentID])
