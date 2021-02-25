# 解析时表权限

MLSQL在拿到脚本时，会解析脚本，并且得到一些表信息，从而可以在实际运行前，就完成一轮权限校验，避免运行了大半天，突然告诉你，你某个表没有权限而引起的错误。尽可能将错误前置，减少用户等待，提升体验。

为了开启并且使用该功能，用户在和Engine的交互过程中，需要设置请求参数`skipAuth` 为false(默认为true)。也就是权限校验是针对每次请求可设置的。更多参数可参考[/run/script 接口](http://docs.mlsql.tech/mlsql-stack/api/run-script.html)。

# 基本原理

用户可以定制自己的权限校验规则。譬如用户开发了一个权限校验jar包，该jar包必须有一个类实现`streaming.dsl.auth.TableAuth`接口，并且在启动Engine时通过参数`spark.mlsql.auth.implClass`指定该实现类的全路径。

用户也可以在向Engine发起请求时，通过`context.__auth_client__`参数设置上面的实现类的全路径。

# 示例实现

MLSQL内置了一个实现，类名为`streaming.dsl.auth.client.MLSQLConsoleClient`,他实现了`TableAuth`接口。 Engine会在解析MLSQL脚本后，调用`MLSQLConsoleClient.auth`方法。 在`auth`方法中，我们会将获得的表信息通过http请求发送给 MLSQL Console服务器。 对应的MLSQL Console处理逻辑在Console项目中的`tech.mlsql.api.controller.TableAuthController`。 

## 权限控制表格式

Engien在调用`MLSQLConsoleClient.auth`进行权限校验的时候，传递给该方法的数据格式是一个List,List内元数数据格式如下：

```scala
case class MLSQLTable(
                       db: Option[String],
                       table: Option[String],
                       columns: Option[Set[String]],
                       operateType: OperateType,
                       sourceType: Option[String],
                       tableType: TableTypeMeta) {
  def tableIdentifier: String = {
    if (db.isDefined && table.isDefined) {
      s"${db.get}.${table.get}"
    } else if (!db.isDefined && table.isDefined) {
      table.get
    } else {
      ""
    }
  }
}

case class TableTypeMeta(name: String, includes: Set[String])
```

其中operateType表示操作类型，支持的操作类型如下：

```scala
object OperateType extends Enumeration {
  type OperateType = Value
  val SAVE = Value("save")
  val LOAD = Value("load")
  val DIRECT_QUERY = Value("directQuery")
  val CREATE = Value("create")
  val DROP = Value("drop")
  val INSERT = Value("insert")
  val UPDATE = Value("update")
  val SELECT = Value("select")
  val SET = Value("set")
  val EMPTY = Value("empty")

  def toList = {
    List(SAVE.toString, LOAD.toString, DIRECT_QUERY.toString,
      CREATE.toString, DROP.toString, INSERT.toString, UPDATE.toString,
      SELECT.toString, SET.toString, EMPTY.toString)
  }
}

```

tableType为数据类型，支持种类如下：

```scala
object TableType {
  val HIVE = TableTypeMeta("hive", Set("hive"))
  val CUSTOME = TableTypeMeta("custom", Set("custom"))
  val BINLOG = TableTypeMeta("binlog", Set("binlog"))
  val HBASE = TableTypeMeta("hbase", Set("hbase"))
  val HDFS = TableTypeMeta("hdfs", Set("parquet",
    "binlogRate", "json", "csv", "image",
    "text", "xml", "excel", "libsvm", "delta", "rate", "streamParquet"))
  val HTTP = TableTypeMeta("http", Set("http"))
  val JDBC = TableTypeMeta("jdbc", Set("jdbc", "streamJDBC"))
  val ES = TableTypeMeta("es", Set("es"))
  val REDIS = TableTypeMeta("redis", Set("redis"))
  val KAFKA = TableTypeMeta("kafka", Set("kafka", "kafka8", "kafka9", "adHocKafka"))
  val SOCKET = TableTypeMeta("socket", Set("socket"))
  val MONGO = TableTypeMeta("mongo", Set("mongo"))
  val SOLR = TableTypeMeta("solr", Set("solr", "streamSolr"))
  val TEMP = TableTypeMeta("temp", Set("temp", "jsonStr", "script", "csvStr", "mockStream", "console", "webConsole"))
  val API = TableTypeMeta("api", Set("mlsqlAPI", "mlsqlConf"))
  val WEB = TableTypeMeta("web", Set("crawlersql"))
  val GRAMMAR = TableTypeMeta("grammar", Set("grammar"))
  val SYSTEM = TableTypeMeta("system", Set("_mlsql_", "model", "modelList", "modelParams", "modelExample", "modelExplain"))
  val UNKNOW = TableTypeMeta("unknow", Set("unknow"))
```

tableType假设是jdbc,那么sourceType可以进一步区分，比如是mysql还是oracle等等。


为了避免繁琐的配置，在`TableAuthController`的实现中，我们添加了一些白名单，比如tableType是`TableType.TEMP`,"custom","binlog","system"亦或者`TableType.HDFS`且是在用户主目录里的，都默认过权限。因为用户有权使用自己的主目录。

同时对于一些特殊tableName,比如`__resource_allocate__`,以`mlsql_temp_`开头的，我们也都可以默认允许，因为这些可能是系统自动产生的一些类型，不开放给用户，可能会导致某些功能缺失。
