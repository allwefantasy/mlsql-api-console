# 会话隔离/并发执行/调试执行/定时任务

标题有点长。我们知道，在Console中，我们是可以选中几条语句执行的。并且系统会记住之前执行语句的结果（表名）。
这显然是很方便我们调试的。在Console中，用户之间写的临时表名都是隔离的。

比如 A用户执行了如下语句：

```
select "a" as userName as table1;
```

同时，B用户执行了如下语句：

```
select "b" as userName as table1;
```

尽管他们用了相同的表名table1,但是并不会互相影响。

但是，如果你写了很多定时执行的脚本，亦或是对外提供API服务，这个时候一般都是适用同一个用户名，这个时候就会使得脚本之间的表名互相影响。
MLSQL Engine的HTTP接口提供了两个参数控制隔离：

1.  如果`sessionPerUser`设置为true 按用户进行隔离
2.  如果`sessionPerRequest`设置为true,那么会按请求隔离。

所以如果你使用定时任务那么请将这两个参数都设置为true。更多参数请参看：[MLSQL Engine Rest接口详解](http://docs.mlsql.tech/mlsql-console/api/run-script.html)