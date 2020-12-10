# 加载JDBC(如MySQL，Oracle)数据常见困惑


MLSQL可以使用Load语法加载支持JDBC协议的数据源。比如MySQL,Oracle.
通常使用方式如下(例子来自[官方文档](http://docs.mlsql.tech/mlsql-console/datasource/jdbc.html))：


```sql
set user="root";

connect jdbc where
url="jdbc:mysql://127.0.0.1:3306/wow?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false"
and driver="com.mysql.jdbc.Driver"
and user="${user}"
and password="${password}"
as db_1;

load jdbc.`db_1.table1` as table1;
select * from table1 as output;

```

其中，connect看着好像是去链接一个数据库，但其实并不是，该语句并不会执行真正的连接动作，
而是仅仅记下了数据库的连接信息，并且给这个真实的数据库（在上面的例子中是DB wow）取了一个别名`db_1`.

之后，如果你需要加载wow库的表，就可以直接使用db_1引用了。

## load会把数据都加载到MLSQL引擎的内存里么？

答案是不会。引擎会批量到MySQL拉取数据进行计算。同一时刻，只有一部分数据在引擎内存里。

## load的时候可以加载过滤条件么
可以，但是没有必要。用户可以把条件直接在后续接select语句中，select 语句里的where条件会被下推给存储做过滤，避免大量数据传输。

## count非常慢，怎么办？

比如用户执行如下语句想查看下表的数据条数,如果表比较大，可能会非常慢，甚至有可能导致Engine有节点挂掉
```
load jdbc.`db_1.tblname` as tblname;
select count(*) from tblname as newtbl;
```

原因是引擎需要拉取全量数据（批量拉取，并不是全部加载到内存），然后计数，而且默认是单线程，一般数据库对全表扫描都比较慢，所以
没有where条件的count可能会非常慢，甚至跑不出来。

如果用户仅仅是为了看下表的大小，推荐用directQuery模式，directQuery会把查询直接发给数据库执行，然后把得到的计算结果返回给引擎,所以非常快。
具体操作方式如下：

```
load jdbc.`db_1.tblname` where directQuery='''
    select count(*) from tblname
''' as newtbl;
```

## 在select语句中加了where条件也很慢（甚至引擎挂掉）

虽然你加了where条件，但是过滤效果可能并不好，引擎仍然需要拉取大量的数据进行计算，引擎默认是单线程的。我们可以配置多线程的方式去数据库拉取数据，可以避免单线程僵死。

核心参数有如下：

1. partitionColumn 按哪个列进行分区
2. lowerBound, upperBound,  分区字段的最小值，最大值（可以使用directQuery获取）
3. numPartitions 分区数目。一般8个线程比较合适。

能够进行分区的字段要求是数字类型，推荐使用自增id字段。

## 多线程拉取还是慢，有办法进一步加速么

你可以通过上面的方式将数据保存到delta/hive中，然后再使用。这样可以一次同步，多次使用。如果你没办法接受延迟，那么可以使用MLSQL把MySQL实时同步到
Delta中，可以参考[MySQL Binlog同步](http://docs.mlsql.tech/mlsql-console/datahouse/binlog.html)

## 有么有深入原理的文章？

可以参考这篇[MLSQL加载JDBC数据源深度剖析](https://mp.weixin.qq.com/s/zaz8sRdIkQEUn65FPQfIQg)

## 结束语

对无论是JDBC类数据源，还是Hive等传统数仓类数据源，MLSQL未来会统一提供索引服务来进行加速，敬请期待。