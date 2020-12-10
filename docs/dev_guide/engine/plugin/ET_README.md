# ET插件开发

ET 插件是MLSQL里最重要的一种插件，能完成非常多的复杂任务，包括：

1. 无法用SQL实现的特定的数据处理
2. 实现各种可复用的复杂的算法模型以及特征工程工具
3. 提供各种便利工具，比如发送邮件，生成图片等各种必须的工具

比如,计算[父子关系](http://docs.mlsql.tech/mlsql-console/process/estimator_transformer/TreeBuildExt.html) 就没办法很好的用SQL表达，
这个时候我们就可以用ET来实现：

```sql
-- 准备模拟数据
set jsonStr = '''
{"id":0,"parentId":null}
{"id":1,"parentId":null}
{"id":2,"parentId":1}
{"id":3,"parentId":3}
{"id":7,"parentId":0}
{"id":199,"parentId":1}
{"id":200,"parentId":199}
{"id":201,"parentId":199}
''';
load jsonStr.`jsonStr` as data;

-- 对模拟数据开始构建父子关系
run data as TreeBuildExt.``
where idCol="id"
and parentIdCol="parentId"
and treeType="nodeTreePerRow"
as result;
```

在MLSQL中，所有以run/train/predict开头的语句，都是通过ET插件实现的。在上边的示例中，其语句的内在含义为：

对表data进行处理，处理的模块是`TreeBuildExt`,where 条件后面是该模块处理data数据需要配置的一些参数，最后模块处理完成后得到一张新表，叫result.
可见  `TreeBuildExt`本质实现了从表到表的数据转换。

对于算法而言，例子就更多了，几乎所有高阶内置算法都是通过ET插件实现的，譬如[随机森林](http://docs.mlsql.tech/mlsql-console/algs/random_forest.html).

[发送邮件](http://docs.mlsql.tech/mlsql-console/process/estimator_transformer/SendMessage.html)的例子：

```sql
set EMAIL_TITLE = "这是邮件标题";
set EMAIL_BODY = `select download_url from t1` options type = "sql";
set EMAIL_TO = "";

select "${EMAIL_BODY}" as content as data;

run data as SendMessage.``
where method="mail"
and to = "${EMAIL_TO}"
and subject = "${EMAIL_TITLE}"
and smtpHost = "xxxxxxxx";
```

MLSQL还允许将这些模块封装成命令使用，比如常见的`!hdfs`命令其实就是一个HDFS ET模块。只是使用命令行代替了run语法而已。
在下面的章节中，我们会分别介绍如何开发ET组件以及如何将ET组件封装成命令行使用。


