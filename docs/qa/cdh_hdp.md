# CDH/HDP 怎么运行MLSQL Engine

CDH/HDP默认都会自带一个Spark发行版，但这个自带的要么太旧，要么太新，甚至还有魔改。
通常，我们建议大家直接使用社区的版本。

那么怎么将Spark社区版运行在CDH/HDP上呢？版本有什么要求么？

选择Spark社区版时，你要考虑如下几个需求：

1. 你CDH/HDP的Hadoop版本是多少？ 2.7还是3.x.根据对应的版本选择合适的Spark社区版。
2. MLSQL目前测试过2.4.3/3.0.0两个版本，所以最好你的Spark版本也要是这两个版本。

这两个因素决定了你现在的Spark社区发行版。

你可以在任何一台和CDH/DP集群网络互通的机器上，下载Spark发行版和MLSQL发行版。然后，你唯一需要做的就是：

> 将Hadoop的配置文件，如core-site.xml,hdfs.xml,hive.xml等文件放到SPARK_HOME/conf目录里

有了这些文件，Spark才知道如何连接Hadoop集群。

接着，就可以参考[安装部署文档](http://docs.mlsql.tech/mlsql-console/howtouse/engine/)去部署了。

## 可能存在的问题

CDH/HDP 根据版本不同，会在Yarn节点上默默的带上一些默认Jar包，**可能**会发生Jar包冲突。
对于HDP大家可以参考这篇文章[Ambari hdp Spark多版本兼容](http://docs.mlsql.tech/zh/develop/ambari_multi_spark.html)获得一些思路.

对于Spark运行在如 CDH,比如比较早的版本如5.13.1，可能需要解决一些问题，大家可以根据实际的错误按图索骥：


### HDFS Kerberos token过期问题

错误信息大概是这样：

```
spark hadoop.security.token.SecretManager token can't be found in cache
```

原因是打包使用hadoop–2.7官方是没有解决这方面问题的(线上也是使用cdh的hadoop)

解决方法：打包指定cdh版本的hadoop依赖，指定-Dhadoop.version=2.6.0-cdh5.13.1    

### HDFS native snappy library not available问题

错误信息大概是这样：

```
native snappy library not available: this version of libhadoop was built without snappy support.
```

解决方法：在SPARK_HOME/conf下的spark-env.sh 中

指定JAVA_LIBRARY_PATH为native so文件路径

```shell
export JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH:\
/opt/cloudera/parcels/CDH/lib/hadoop/lib/native

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:\
/opt/cloudera/parcels/GPLEXTRAS/lib/hadoop/lib/native:\
/opt/cloudera/parcels/CDH/lib/hadoop/lib/native

export SPARK_DIST_CLASSPATH=/etc/hadoop/conf:\
/opt/cloudera/parcels/CDH/lib/hadoop/lib/*:\
/opt/cloudera/parcels/CDH/lib/hadoop/lib/native/*:\
/opt/cloudera/parcels/GPLEXTRAS/lib/hadoop/lib/*::
```

executor端,启动脚本添加java.library.path为native so文件路径

```shell
ENV SPARK_EXECUTOR_NATIVE_JAVA_OPTS="-Djava.library.path=/opt/cloudera/parcels/CDH/lib/hadoop/lib/native/"

executor)
    shift 1
    CMD=(
      ${JAVA_HOME}/bin/java
      "${SPARK_EXECUTOR_NATIVE_JAVA_OPTS[@]}"
      "${SPARK_EXECUTOR_JAVA_OPTS[@]}"
      -Xms$SPARK_EXECUTOR_MEMORY
      -Xmx$SPARK_EXECUTOR_MEMORY
      -cp "$SPARK_CLASSPATH:$SPARK_DIST_CLASSPATH"
      org.apache.spark.executor.CoarseGrainedExecutorBackend
      --driver-url $SPARK_DRIVER_URL
      --executor-id $SPARK_EXECUTOR_ID
      --cores $SPARK_EXECUTOR_CORES
      --app-id $SPARK_APPLICATION_ID
      --hostname $SPARK_EXECUTOR_POD_IP
    )
    ;;
```

### [https访问异常](https://github.com/oracle/graal/issues/493)

错误信息大概是这样：`PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException`
原因是：访问https服务需要指定证书
解决方法：
将安全证书安装后， 添加至本地Jre  security中，

```shell
cd /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/security 目录下执行:

keytool -import  -v -alias xxxxx -keystore
"/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/security/cacerts"
-file "/xxx/xxxx/xxxxx.cer"
```


### Spark3和hadoop兼容问题

Spark 3.0 官方默认支持的Hadoop最低版本为2.7, Hive最低版本为 1.2。我们平台使用的CDH 5.13,对应的版本分别为hadoop-2.6.0, hive-1.1.0。
所以在编译yarn模块会报错：
具体是/resource-managers/yarn/src/main/scala/org/apache/spark/deploy/yarn/Client.scala的
298行logAggregationContext.setRolledLogsIncludePattern(includePattern)
300行logAggregationContext.setRolledLogsExcludePattern(excludePattern)问题

由于这两个方法是hadoop2.6.4添加的，如果你的hadoop版本低于2.6.4，那么编译就会报错。

解决方法：参考 https://github.com/apache/spark/pull/16884/files

### Spark 3.0 整合hive异常(高版本hive忽略)

错误信息大概是这样：

```
hive.ql.metadata.HiveException: Unable to fetch table Invalid method name: get_table_req
```

原因是spark3源码默认使用2.3.7版本的hive，里面的getTable方法调用了get_table_req,而hive客户端的版本<=1.2.x找不到get_table_req的方法，
所以报了Invalid method name:get_table_req

解决方法：自己手动编译Spark，指定hive的版本为1.2.1，打包编译Spark的时候指定-Phive-1.2


