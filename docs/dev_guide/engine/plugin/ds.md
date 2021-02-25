# 自定义数据源插件开发

数据源主要应用于MLSQL的Load/Save语法里。尽管MLSQL提供了非常多的数据源支持[加载和存储多种数据源](http://docs.mlsql.tech/mlsql-stack/datasource/)，但肯定还有非常的数据源并没有被官方支持到。MLSQL为此提供了自定义数据源的支持。

通常，为了达成此目标，用户大体需要实现两个步骤：

1. 按Spark DataSource标准封装对应的数据源。因为Spark良好的生态储备，一般而言大部分数据源都会有Spark的Connector(DataSource)。 所以这一步实际上仅仅是引入相应的Connector Jar包即可。
2. 按MLSQL DataSource标准进一步封装Spark DataSource数据源（或者原生的数据源）。比如我么常用的jsonStr,csvStr等就没有使用Spark DataSource Connector,而是职级使用MLSQL DataSource标准实现的。

在这篇教程中，我们不会介绍Spark DataSource的开发，而是介绍MLSQL DataSource的标准。

## MLSQL Excel 数据源介绍

加载或者保存Excel会是一个较为常见的操作，我们在[mlsql-plugins](https://github.com/allwefantasy/mlsql-plugins/tree/master/mlsql-excel)实现了excel在MLSQL中的读取和保存。

使用如下：

```sql
load excel.`/tmp/upload/example_en.xlsx` 
where useHeader="true" and 
maxRowsInMemory="100" 
and dataAddress="A1:C8"
as data;
```

保存如下：

```sql
select 1 as as as b;
save overwrite b as excel.`/tmp/b.xlsx` where header="true";
```

现在我们来看看如何进行开发。

## Excel数据源开发

要实现一个数据源的开发，需要实现如下接口：

1. MLSQLSource 读取操作
2. MLSQLSink 写入操作
3. MLSQLSourceInfo 权限校验信息的生成
4. MLSQLRegistry 注册数据源名称
5. VersionCompatibility 版本兼容

1，2必须实现其中一个，3可选，4必须。

MLSQLSource的签名：

```scala
trait MLSQLSource extends MLSQLDataSource with MLSQLSourceInfo {
  def load(reader: DataFrameReader, config: DataSourceConfig): DataFrame
}
```

MLSQLSink的签名:

```scala
trait MLSQLSink extends MLSQLDataSource {
  def save(writer: DataFrameWriter[Row], config: DataSinkConfig): Any
}
```

MLSQLSourceInfo的签名：

```scala
trait MLSQLSourceInfo extends MLSQLDataSource {
  def sourceInfo(config: DataAuthConfig): SourceInfo

  def explainParams(spark: SparkSession): DataFrame = {
    import spark.implicits._
    spark.createDataset[String](Seq()).toDF("name")
  }
}
```

不过通常都会有一些基类，简化我们的操作。比如如果你实现的是文件类操作，那么就可以选用`streaming.core.datasource.MLSQLBaseFileSource`作为实现的基类。MLSQLExcel也会选择该类作为基类。

下面是签名：

```scala
class MLSQLExcel(override val uid: String)
  extends MLSQLBaseFileSource
    with WowParams with VersionCompatibility {
  def this() = this(BaseParams.randomUID())
```


我们先来看看如何Register我们的数据源：

```scala
 override def register(): Unit = {
    DataSourceRegistry.register(MLSQLDataSourceKey(fullFormat, MLSQLSparkDataSourceType), this)
    DataSourceRegistry.register(MLSQLDataSourceKey(shortFormat, MLSQLSparkDataSourceType), this)
  }

  override def fullFormat: String = "com.crealytics.spark.excel"

  override def shortFormat: String = "excel"
```
定义fullFormat，shortFormat。 fullFormat是Spark数据源的类型。shortFormat则是你给这个数据源取的短名。这样我们可以在load/save语句中直接使用excel。

register方法会将相关信息注册到一个统一的地方。

接着是提供权限校验的一些必要信息：

```
override def sourceInfo(config: DataAuthConfig): SourceInfo = {
    val context = ScriptSQLExec.contextGetOrForTest()
    val owner = config.config.get("owner").getOrElse(context.owner)
    SourceInfo(shortFormat, "", resourceRealPath(context.execListener, Option(owner), config.path))
  }
```

其实就是要拼装出一个SourceInfo对象，这个对象会交给校验服务器进行校验。

最后是版本兼容,你需要明确指定兼容哪些版本的MLSQL。

```scala
override def supportedVersions: Seq[String] = {
    Seq("1.5.0-SNAPSHOT", "1.5.0", "1.6.0-SNAPSHOT", "1.6.0", "2.0.0", "2.0.1", "2.0.1-SNAPSHOT","2.1.0-SNAPSHOT",
      "2.1.0")
  }
```

在MLSQL Excel这个示例中，load/save完全交给基类就好了。如果有特殊需求，可由覆盖基类。我们来看看基类是如何实现数据加载的。具体代码如下：

```scala
override def load(reader: DataFrameReader, config: DataSourceConfig): DataFrame = {
    val context = ScriptSQLExec.contextGetOrForTest()
    val format = config.config.getOrElse("implClass", fullFormat)
    val owner = config.config.get("owner").getOrElse(context.owner)
    reader.options(rewriteConfig(config.config)).format(format).load(resourceRealPath(context.execListener, Option(owner), config.path))
  }
```

通过`ScriptSQLExec`对象可以获取一个context对象，该对象可当前HTTP请求的所有请求参数。 `config: DataSourceConfig`则包含了 load where条件里的所有参数，对应的是：

```
useHeader="true" and 
maxRowsInMemory="100" 
and dataAddress="A1:C8"
```
接着将这些参数设置到reader里，并且通过resourceRealPath解析到excel文件的实际路径，因为我们有主目录的概念。这里是标准的DataFrame 读 API了。

相应的，save操作也是类似的：

```sql
 override def save(writer: DataFrameWriter[Row], config: DataSinkConfig): Any = {
    val context = ScriptSQLExec.contextGetOrForTest()
    val format = config.config.getOrElse("implClass", fullFormat)
    val partitionByCol = config.config.getOrElse("partitionByCol", "").split(",").filterNot(_.isEmpty)
    if (partitionByCol.length > 0) {
      writer.partitionBy(partitionByCol: _*)
    }
    writer.options(rewriteConfig(config.config)).mode(config.mode).format(format).save(resourceRealPath(context.execListener, Option(context.owner), config.path))
  }
```

当然了，用户也可以完全实现自己的逻辑，比如读取一个图片什么的，直接使用HDFS API 去读取即可。可以使用我们封装好的`tech.mlsql.common.utils.hdfs.HDFSOperator`读取。

这段代码是保存图片例子：

```scala
override def save(writer: DataFrameWriter[Row], config: DataSinkConfig): Unit = {
    val context = ScriptSQLExec.contextGetOrForTest()
    val baseDir = resourceRealPath(context.execListener, Option(context.owner), config.path)

    if (HDFSOperator.fileExists(baseDir)) {
      if (config.mode == SaveMode.Overwrite) {
        HDFSOperator.deleteDir(baseDir)
      }
      if (config.mode == SaveMode.ErrorIfExists) {
        throw new MLSQLException(s"${baseDir} is exists")
      }
    }

    config.config.get(imageColumn.name).map { m =>
      set(imageColumn, m)
    }.getOrElse {
      throw new MLSQLException(s"${imageColumn.name} is required")
    }

    config.config.get(fileName.name).map { m =>
      set(fileName, m)
    }.getOrElse {
      throw new MLSQLException(s"${fileName.name} is required")
    }

    val _fileName = $(fileName)
    val _imageColumn = $(imageColumn)

    val saveImage = (fileName: String, buffer: Array[Byte]) => {
      HDFSOperator.saveBytesFile(baseDir, fileName, buffer)
      baseDir + "/" + fileName
    }

    config.df.get.rdd.map(r => saveImage(r.getAs[String](_fileName), r.getAs[Array[Byte]](_imageColumn))).count()
  }
```

## 使用

开发完成后，就可以打包了了。你可以选择[在线或者离线安装](http://docs.mlsql.tech/mlsql-stack/plugin/)。因为是自己开发的，一般都选择离线安装。








