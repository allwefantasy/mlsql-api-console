# 使用 MLSQL 语言编写 Spark 程序

使用场景：

用户希望能够使用 MLSQL 语言替换 Java/Scala/PySpark 去写 Spark 程序，除此之外，他希望引擎的提交方式要和传统的 Spark 完全类似，执行完成后就自动退出，因此，对于想通过 MLSQL 程序直接使用 Spark 去提交 MLSQL 脚本可参考下列使用命令。

一个典型的启动命令：

```shell
$SPARK_HOME/bin/spark-submit --class streaming.core.StreamingApp \
        --driver-memory ${DRIVER_MEMORY} \
        --jars ${JARS} \
        --master local[*] \
        --name mlsql \        
        --conf "spark.scheduler.mode=FAIR" \
       [1] ${MLSQL_HOME}/libs/${MAIN_JAR}    \ 
        -streaming.name mlsql    \
        -streaming.platform spark   \
        -streaming.rest false   \
        -streaming.spark.service false \
        -streaming.thrift false \
        -streaming.enableHiveSupport true \
        -streaming.mlsql.script.path=${MLSQL_PATH} \
        -streaming.runtime_hooks=tech.mlsql.runtime.SparkSubmitMLSQLScriptRuntimeLifecycle \
        -streaming.mlsql.script.owner=${owner} \
        -streaming.mlsql.sctipt.jobName=${jobName}
```

## 相关参数

|              参数              |           说明            |                            示例值                            |
| :----------------------------: | :-----------------------: | :----------------------------------------------------------: |
|         streaming.rest         |    是否开启 http 接口     |                   布尔值，需要设置为 false                   |
|    streaming.spark.service     |    执行完是否退出程序     |                true 不退出  <br />false 退出                 |
|    streaming.runtime_hooks     | 指定执行mlsql脚本的扩展类 |  tech.mlsql.runtime.SparkSubmitMLSQLScriptRuntimeLifecycle   |
|  streaming.mlsql.script.path   |  指定执行 mlsql 脚本路径  | file:///Users/xxx/Documents/mlsql.txt (本地)<br />hdfs:///tmp/mlsql.txt (HDFS)<br />http://localhost:8080/mlsql.txt |
|  streaming.mlsql.script.owner  |     指定脚本执行用户      |                         admin (默认)                         |
| streaming.mlsql.sctipt.jobName |       指定任务名称        |        SparkSubmitMLSQLScriptRuntimeJob（默认）&nbsp;        |

