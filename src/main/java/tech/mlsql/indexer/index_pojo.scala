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

case class MlsqlIndexerWrapper(name: String,
                        oriFormat: String,
                        oriPath: String,
                        oriStorageName: String,
                        format: String,
                        path: String,
                        storageName: String,
                        status: Int,
                        owner:String,
                        lastStatus: Int,
                        lastFailMsg: String,
                        lastExecuteTime: Long,
                        syncInterval: Long,
                        content: String,
                        indexerConfig: String,
                        lastJobId: String,
                        indexerType: String
                       )
case class MlsqlOriTable(name:String,format:String,path:String,storageName:String,options:Map[String,String])