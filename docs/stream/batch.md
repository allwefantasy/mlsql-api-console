# 如何对流的结果以批的形式保存

MLSQL对流的数据源支持有限。如果我想流的数据保存到ES中，但是没有相应的流式ES数据源的实现该怎么办？为了解决这个问题，
MLSQL提供了一个'custom' 流式数据源，可以方便的让你用批的方式操作流的结果数据。

我们来看看具体的示例代码：

```sql
-- the stream name, should be uniq.
set streamName="streamExample";


-- mock some data.
set data='''
{"key":"yes","value":"no","topic":"test","partition":0,"offset":0,"timestamp":"2008-01-24 18:01:01.001","timestampType":0}
{"key":"yes","value":"no","topic":"test","partition":0,"offset":1,"timestamp":"2008-01-24 18:01:01.002","timestampType":0}
{"key":"yes","value":"no","topic":"test","partition":0,"offset":2,"timestamp":"2008-01-24 18:01:01.003","timestampType":0}
{"key":"yes","value":"no","topic":"test","partition":0,"offset":3,"timestamp":"2008-01-24 18:01:01.003","timestampType":0}
{"key":"yes","value":"no","topic":"test","partition":0,"offset":4,"timestamp":"2008-01-24 18:01:01.003","timestampType":0}
{"key":"yes","value":"no","topic":"test","partition":0,"offset":5,"timestamp":"2008-01-24 18:01:01.003","timestampType":0}
''';

-- load data as table
load jsonStr.`data` as datasource;

-- convert table as stream source
load mockStream.`datasource` options 
stepSizeRange="0-3"
as newkafkatable1;

-- aggregation 
select cast(value as string) as k  from newkafkatable1
as table21;

-- run command as  MLSQLEventCommand.`` where
--       eventName="started,progress,terminated"
--       and handleHttpUrl="http://127.0.0.1:9002/jack"
--       and method="POST"
--       and params.a=""
--       and params.b="";
!callback post "http://127.0.0.1:9002/api_v1/test" when "started,progress,terminated";
-- output the the result to console.


save append table21  
as custom.`` 
options mode="append"
and duration="15"
and sourceTable="jack"
and code='''
select count(*) as c from jack as newjack;
save append newjack as parquet.`/tmp/jack`; 
'''
and checkpointLocation="/tmp/cpl15";
```

我们关注点放在最后一句：

```sql
save append table21  
as custom.`` 
options mode="append"
and duration="15"
and sourceTable="jack"
and code='''
select count(*) as c from jack as newjack;
save append newjack as parquet.`/tmp/jack`; 
'''
and checkpointLocation="/tmp/cpl15";
```

有几个点需要注意：

1. 数据源名称是 custom
2. 我们需要将结果表通过sourceTable重新取名，这里我们把table21取名为jack,然后在子代码中使用。
3. code里允许你用批的形态操作jack表。

这样，我们就能很方便的将大部分数据写入到支持批的数据源中了。

## Hive分区表写入

如果我们希望把数据写入hive分区表怎么办？依然只要修改最后一句。如果是动态分区，
可以按如下方式写：


```sql
save append table21
as custom.``
options mode="append"
and duration="15"
and sourceTable="jack"
and code='''
save append jack as hive.`/tmp/jack` partitionBy 【partitionCol】;
'''
and checkpointLocation="/tmp/cpl15";
```

如果是静态分区，则直接指定目录即可。另外也可以使用hive原生insert语句,例如下面的例子

```
--开启hive相关配置，放在脚本前面
set hive.exec.dynamic.partition=true where type="conf";
set hive.exec.dynamic.partition.mode=nostrict  where type="conf";


save append table21
as custom.``
options mode="append"
and duration="15"
and sourceTable="jack"
and code='''
insert into table db.tb partition(【partitionCol】)
select * from jack;
'''
and checkpointLocation="/tmp/cpl15";

```


