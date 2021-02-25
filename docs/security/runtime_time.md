# 运行时表/列权限

除了前文提到的【解析时权限】，MLSQL还支持运行时表/列权限。

通常我们可能需要过滤或者脱敏一些表的字段。一个比较典型的场景是，比如某种hive表，我压根不希望知道用户看到某个字段，亦或是我希望针对这个用户给这个字段加密。那么运行时表/列权限控制就可以达成类似的效果。

## Load表自定义改写

我们知道，在MLSQL中，所有数据源都需要通过load进行加载才能访问。尽管可能会有人厌烦并且感觉这种方式繁琐，但是面对各种数据源，它又是不可获取的，因为我们无法假设用户只会有一个数仓。在load语法中，我们允许用户拿到实际的数据源，根据特定的条件（比如针对特定的用户）拦截该数据源的数据表示DataFrame,然后修改这个DataFrame,譬如drop掉一些列或者给某些列加密。

为此，用户可以实现`streaming.core.datasource.RewriteableSource`接口，该接口签名如下：

```scala
trait RewriteableSource {
  def rewrite(df: DataFrame,
              config: DataSourceConfig,
              sourceInfo: Option[SourceInfo],
              context: MLSQLExecuteContext): DataFrame
}
```

一旦用户实现了该接口，就可以把自己的实现应用到Engine上了。这要分两步来完成。

1. 通过Engine启动参数`--conf spark.mlsql.enable.datasource.rewrite=true` 来开启该改写
2. 通过Engine启动参数`--conf spark.mlsql.datasource.rewrite.implClass=YOUR CLASS FULL NAME`来指定实现了`RewriteableSource`接口的类。

这样，用户就可以根据自己的意图来控制用户加载表所能得到的实际数据了。

## select语句控制

你通过Engine启动参数`--conf spark.mlsql.enable.runtime.select.auth=true` 就可以开启select语句更精细的权限控制。然而，这个功能完全可以通过`Load表自定义改写`来完成。 一旦开启后，我们会动态解析select语句中涉及到的表和列，然后将这些表和列，通过【解析时权限】中提到的`TableAuth`实现，来进行权限的校验。注意，该权限控制也是在运行时动态控制的。

## directQuery 语句控制

load语法支持directQuery,可以实现类似聚合操作的完整下推，比如：

```sql
load jdbc.`db_1.test1` where directQuery='''
select * from test1 where a = "b"
''' as newtable;

select * from newtable;
```

其中`select * from test1 where a = "b"` 语句会作为原生的SQL语句下推到JDBC数据源中执行。这个时候用户访问了哪些表，我们其实是不知道的。这个时候，我们可以通过ENgine启动参数`--conf spark.mlsql.enable.runtime.directQuery.auth=true`来开启directQuery权限控制。 值得注意的是，该功能取决于MLSQL中对应的数据源的实现。比如JDBC数据源就实现了针对directQuery的实现，然而用户可能去访问ES，并且也使用了directQuery功能，而ES的MLSQL数据源并没有实现directQuery权限控制，那么及时开启了该功能，依然无法生效。

针对JDBC数据源，一旦开启了该功能，系统就会尝试去解析用户填写的SQL语句，然后得到相关的信息，依然通过实现了`TableAuth`接口来进行校验。