# 常见宏命令

MLSQL内置了非常多的宏命令，可以帮助用户实现更好的交互。

## !show

该命令可以展示系统很多信息。

查看当前引擎版本：

```
!show version;
```

显示show支持的所有子命令：

```
!show commands;
```

列出所有的表：

```
!show tables;
```

从指定db罗列所有表

```
!show tables from [DB名称];
```

列出所有当前正在运行的任务：


```
!show jobs;
```

列出某个任务的相关信息：

```
!show "jobs/v2/[jobGroupId]";
!show "jobs/[jobGroupId]";
!show "jobs/get/[jobGroupId]";
```

三者显示的内容不同，用户可以自己尝试下结果。

列出所有可用的数据源：

```
!show datasources;
```

列出所有Rest接口：

```
!show "api/list";
```

列出所有支持的配置参数（不全,以文档为主）:

```
!show "conf/list";
```

查看日志：

```
!show "log/[文件偏移位置]";
```



列出数据源的参数：

```
!show "datasources/params/[datasource name]";
```

列出当前系统资源：

```
!show resource;
```

列出所有的ET组件：

```
!show et;
```

列出某个ET组件的信息：

```
!show "et/[ET组件名称]";
```

列出所有函数：

```
!show functions;
```

列出某个函数：

```
!show "function/[函数名称]";
```

## !hdfs

!hdfs 主要用来查看文件系统。支持大部分HDFS查看命令。

查看帮助：

```
!hdfs -help;
!hdfs -usage;
```

下面为一些常见操作：

罗列某个目录所有文件：

```
!hdfs -ls /tmp;
```

删除文件目录：


```
!hdfs -rmr /tmp/test;
```

拷贝文件：


```
!hdfs -cp /tmp/abc.txt /tmp/dd;
```

## !kill

该命令主要用来杀任务。

```
!kill [groupId或者Job Name];
```

## !desc

查看表结构。

```
!desc [表名];
```


## !cache/!unCache

对表进行缓存。

```
!cache [表名] [缓存周期];
```

其中缓存周期有三种选择：

1. script
2. session
3. application

手动释放缓存：

```
!unCache [表名];
```

## !if/!elif/!then/!else/!fi

这五者配合使用，可以实现条件分支语句。参看：[MLSQL 支持条件分支语句](http://blog.mlsql.tech/blog/mlsql-ifelse.html).

## !println

打印文本：

```
!println '''文本内容''';
```

## !runScript

将一段文本当做MLSQL脚本执行：

```
!runScript ''' select 1 as a as b; ''' named output;
```

## !last

将上一条命令的输出取一个表名，方便后续使用：

```
!hdfs -ls /tmp;
!last named table1;
select * from table1 as output;
```

## !lastTableName

记住上一个表的名字，然后可以在下次获取：

```
select 1 as a as table1;
!lastTableName;
select "${__last_table_name__}" as tableName as output;
```

输出结果为 table1;

## !tableRepartition

对标进行分区：

```
!tableRepartition _ -i [表名] -num [分区数] -o [输出表名];
```


## !saveFile

如果一个表只有一条记录，并且该记录只有一列，并且该列是binary格式，那么我们可以将该列的内容保存成一个文件。比

```
!saveFile _ -i [表名] -o [保存路径];
```

## !emptyTable

比如有的时候我们并不希望有输出，可以在最后一句加这个语句：

```
!emptyTable;
```

## !profiler

执行原生SQL:

```
!profiler sql ''' select 1 as a ''' ;
```

查看所有spark内核的配置：

```
!profiler conf;
```

查看一个表的执行计划：

```
!profiler explain [表名或者一条SQL];
```


## !python

可以通过该命令设置一些Python运行时环境。

参考: [配置Python以及使用Python代码处理数据](http://docs.mlsql.tech/mlsql-console/python/etl.html#%E9%85%8D%E7%BD%AEpython%E4%BB%A5%E5%8F%8A%E4%BD%BF%E7%94%A8python%E4%BB%A3%E7%A0%81%E5%A4%84%E7%90%86%E6%95%B0%E6%8D%AE)



## !ray

支持python代码集成。参看 [MLSQL Python支持](http://docs.mlsql.tech/mlsql-console/python/)

## !delta

显示帮助：

```
!delta help;
```

列出所有delta表：

```
!delta show tables;
```

版本历史：

```
!delta history [db/table];
```

表信息：

```
!delta info [db/table];
```

文件合并：

```
!delta compact [表路径] [版本号] [文件数] [是否后台运行];
!delta compact db/tablename 100 3 background;
```

上面表示对db/table 100之前的版本的文件进行合并，每个目录只保留三个文件。






## !withWartermark

参考 [window/watermark的使用](http://docs.mlsql.tech/mlsql-console/stream/window_wartermark.html)

## !plugin

插件安装和卸载. 参看：[插件](http://docs.mlsql.tech/mlsql-console/plugin/)

## !kafkaTool

kafka相关的小工具。参看： [MLSQL Kafka小工具集锦](http://docs.mlsql.tech/mlsql-console/stream/kakfa_tool.html)

## !callback

流式Event事件回调。参看： [如何设置流式计算回调](http://docs.mlsql.tech/mlsql-console/stream/callback.html)













