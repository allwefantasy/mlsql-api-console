# ET插件开发

开发ET插件有三种方式：

1. 直接修改MLSQL源码
2. 独立成模块作为内置插件使用
3. 独立成项目作为外置插件使用


## 直接修改MLSQL源码
让我们先来实现一个没啥用的插件，叫 `EmptyTable`,具体用法如下：

```
select 1 as col as table1;
run table1 as EmptyTable.`` as outputTable;
```

EmptyTable啥也不干，返回一个空表,在上面的例子中，空表的名称为outputTable.

具体实现代码如下：

```
package tech.mlsql.plugins.ets

import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.{DataFrame, SparkSession}
import streaming.dsl.mmlib.SQLAlg
import streaming.dsl.mmlib.algs.param.WowParams

class EmptyTable(override val uid: String) extends SQLAlg with WowParams {
  def this() = this(Identifiable.randomUID("tech.mlsql.plugins.ets.EmptyTable"))

  override def train(df: DataFrame, path: String, params: Map[String, String]): DataFrame = {
    df.sparkSession.emptyDataFrame
  }

  override def batchPredict(df: DataFrame, path: String, params: Map[String, String]): DataFrame = train(df, path, params)

  override def load(sparkSession: SparkSession, path: String, params: Map[String, String]): Any = ???

  override def predict(sparkSession: SparkSession, _model: Any, name: String, params: Map[String, String]): UserDefinedFunction = ???
}

```

可以看到，EmptyTable只需要继承SQLAlg和混入WowParams即可（实际上还有很多其他的接口可以混入，比如权限接口等等）。然后覆盖实现相应的方法即可。在MLSQL里，train语法对应的是ET里的train方法。 run/predict对应的是batchPredict方法。register则对应的是load/predict 方法。

我们希望在train/run语法里都可以用，只需要实现train/batchPredict方法即可。

train方法签名也比较简单，给定你一个dataframe(在示例中是table1)，以及一些参数（where条件里的参数），返回一个新的dataframe接口。

```
-- table1 就是train的第一个参数df
run table1 
as EmptyTable.`` 
-- where 在train方法里可以通过params拿到
where ... 
-- outputTable 就是train的返回值
as outputTable;
```

到目前为止，我们就实现了一个没啥用的ET插件了。那么如何注册到MLSQL引擎中呢？如果是作为内置插件，我们只要添加如下一行代码到`tech.mlsql.ets.register.ETRegister`即可：


```
register("EmptyTable", "tech.mlsql.ets.EmptyTable")
```

现在，你启动IDE，就可以使用这个模块了。

## 独立成模块作为内置插件使用

如果你希望这个插件是一个独立的模块，并且内置在MLSQL中，那么你需要在external目录下新建一个模块，我们已经非常多的例子了。

![](http://docs.mlsql.tech/upload_images/5cb2719a-f5a9-46d9-88fc-09f297efdb7a.png)

除了上面这个文件以外，你还需要提供一个`EmptyTableApp`的类（类名随便取）,内容如下：

```
package tech.mlsql.plugins.ets

import tech.mlsql.dsl.CommandCollection
import tech.mlsql.ets.register.ETRegister
import tech.mlsql.version.VersionCompatibility

/**
 * 6/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class EmptyTableApp extends tech.mlsql.app.App with VersionCompatibility {
  override def run(args: Seq[String]): Unit = {
    //注册ET组件
    ETRegister.register("EmptyTable", classOf[EmptyTable].getName)   
    }


  override def supportedVersions: Seq[String] = Seq("1.5.0-SNAPSHOT", "1.5.0", "1.6.0-SNAPSHOT", "1.6.0")
}


object EmptyTableApp {

}

```

然后在`tech.mlsql.runtime.PluginHook` 添加该内置插件：

```scala
object PluginHook extends Logging {
  private val apps = List(
    "tech.mlsql.plugins.app.pythoncontroller.PythonApp",
    "tech.mlsql.plugins.mlsql_watcher.MLSQLWatcher",
    "tech.mlsql.plugins.sql.profiler.ProfilerApp",
    "tech.mlsql.autosuggest.app.MLSQLAutoSuggestApp",
    "tech.mlsql.plugins.ets.ETApp",
    "tech.mlsql.plugins.healthy.App",
    "tech.mlsql.plugins.ets.EmptyTableApp"
  )
```

当然，你还需要在streamingpro-mlsql 添加该该模块依赖。通常添加在profile/streamingpro-spark-2.4.0-adaptor 和profile/streamingpro-spark-3.0.0-adaptor 中都要添加。如果你这个模块只兼容其中一个，添加一个即可。

## 作为外置插件使用

如果你想作为外置插件使用，也就是单独成一个项目开发和维护，可以参考项目 :https://github.com/allwefantasy/mlsql-plugins

模式和内置插件一样，然后打成jar包，使用[离线安装的方式安装](http://docs.mlsql.tech/mlsql-console/plugin/offline_install.html)

MLSQL 外置插件可以动态安装，但是如果要更新，则需要重启服务。






