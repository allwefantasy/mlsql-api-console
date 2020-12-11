package tech.mlsql.indexer

//format
case class MysqlIndexerConfig(
                               name: String,
                               from: String,
                               partitionColumn: String,
                               partitionNum: String,
                               syncInterval: String,
                               engineName:String
                             )

case class LoadStatement(raw: String, format: String, path: String, option: Map[String, String] = Map[String, String](), tableName: String)

case class PartitionBean(
                          upperBound: Long,
                          partitionNumValue: Long,
                          dbName: String,
                          indexerType: String,
                          partitionColumn: String,
                          lowerBound: Long,
                          tableName: String,
                          idCols: String,
                          engineName: String
                        )

case class HiveIndexerConfig(from: String)