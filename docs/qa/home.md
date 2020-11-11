# 个人主目录

MLSQL Engine每个实例要求有一个主目录，该主目录里会为每个登录的账号设置一个子目录。
主目录通常是你在注册引擎时设置：

![](http://docs.mlsql.tech/upload_images/7bbb41d7-f378-413a-a1d4-c079ba231205.png)


我们以test2为例，此时主目录是/data/mlsql/homes（并且假设用户使用了分布式存储HDFS）,那么用户需要手工创建该目录（启动Engine的账号需要能够读写该目录）。
如果有用户A save文件，比如:

```sql
save overwrite table1 as parquet.`/tmp/table1`;
```

此时在HDFS实际的存储目录为：

```
/data/mlsql/homes/A/tmp/table1
```

同理，A用户上传的文件也会存储在 `/data/mlsql/homes/A/tmp/upload` 中。

尽管实际路径如此，但是在MLSQL中，使用者是无需关心这个前缀的。比如如果你要列出所有上传的文件，可以使用：

```
!hdfs -ls /tmp/upload;
```

而不是完整路径。

