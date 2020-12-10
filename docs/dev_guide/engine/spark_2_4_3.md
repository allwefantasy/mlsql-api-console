# Spark 2.4.3开发环境

## Profile设置
项目导入后，核心在于Profile的设置。在右侧Maven选项里，对如下Profile 进行勾选即可：

![](http://docs.mlsql.tech/upload_images/def289d4-1e3b-4883-800e-5b113db0b872.png)


勾选完成后，项目结构如下：

![](http://docs.mlsql.tech/upload_images/76d22b09-e634-4b3f-8ef1-8ef2c61bfbbf.png)

## 在IDE中启动调试

`streamingpro-mlsql` 模块是主模块，他依赖其他所有模块。实际上，external里部分模块对`streamingpro-mlsq`模块有循环依赖的问题，我们通过在external里设置为Provided 来解决这个问题。

所以要在IDE中直接进行启动和调试，只要在`streamingpro-mlsql`新建一个启动类即可。下面是我使用的一个启动类：

```scala
package streaming.core

/**
 * 2019-03-20 WilliamZhu(allwefantasy@gmail.com)
 */
object WilliamLocalSparkServiceApp {
  def main(args: Array[String]): Unit = {
    StreamingApp.main(Array(
      "-streaming.master", "local[*]",
      "-streaming.name", "god",
      "-streaming.rest", "true",
      "-streaming.thrift", "false",
      "-streaming.platform", "spark",
      "-spark.mlsql.enable.runtime.directQuery.auth", "true",
//      "-streaming.ps.cluster.enable","false",
      "-streaming.enableHiveSupport","false",
      "-spark.mlsql.datalake.overwrite.hive", "true",
      "-spark.mlsql.auth.access_token", "mlsql",
      //"-spark.mlsql.enable.max.result.limit", "true",
      //"-spark.mlsql.restful.api.max.result.size", "7",
      //      "-spark.mlsql.enable.datasource.rewrite", "true",
      //      "-spark.mlsql.datasource.rewrite.implClass", "streaming.core.datasource.impl.TestRewrite",
      //"-streaming.job.file.path", "classpath:///test/init.json",
      "-streaming.spark.service", "true",
      "-streaming.job.cancel", "true",
      "-streaming.datalake.path", "/data/mlsql/datalake",

      "-streaming.plugin.clzznames","tech.mlsql.plugins.ds.MLSQLExcelApp",

      // scheduler
      "-streaming.workAs.schedulerService", "false",
      "-streaming.workAs.schedulerService.consoleUrl", "http://127.0.0.1:9002",
      "-streaming.workAs.schedulerService.consoleToken", "mlsql",


//      "-spark.sql.hive.thriftServer.singleSession", "true",
      "-streaming.rest.intercept.clzz", "streaming.rest.ExampleRestInterceptor",
//      "-streaming.deploy.rest.api", "true",
      "-spark.driver.maxResultSize", "2g",
      "-spark.serializer", "org.apache.spark.serializer.KryoSerializer",
//      "-spark.sql.codegen.wholeStage", "true",
      "-spark.ui.allowFramingFrom","*",
      "-spark.kryoserializer.buffer.max", "2000m",
      "-streaming.driver.port", "9003"
//      "-spark.files.maxPartitionBytes", "10485760"

      //meta store
//      "-streaming.metastore.db.type", "mysql",
//      "-streaming.metastore.db.name", "app_runtime_full",
//      "-streaming.metastore.db.config.path", "./__mlsql__/db.yml"

      //      "-spark.sql.shuffle.partitions", "1",
      //      "-spark.hadoop.mapreduce.job.run-local", "true"

      //"-streaming.sql.out.path","file:///tmp/test/pdate=20160809"

      //"-streaming.jobs","idf-compute"  
      //"-streaming.driver.port", "9005"
      //"-streaming.zk.servers", "127.0.0.1",
      //"-streaming.zk.conf_root_dir", "/streamingpro/jack"
    ))
  }
}

```

用户可以随时自己设置很多参数。点击右键即可Run/Debug。 注意，在IDE调试，并不需要Spark环境。

## 自助构建发行版

用户可以拷贝黏贴（修改）如下脚本完成发行版的构建。

```shell
export LC_ALL=zh_CN.UTF-8
export LANG=zh_CN.UTF-8
# 依赖的Spark版本
export MLSQL_SPARK_VERSION=2.4
# scala版本
export SCALA_VERSION=2.11
# 项目根目录
export MLSQL_M_HOME=/Users/allwefantasy/CSDNWorkSpace/streamingpro-spark-2.4.x
# 版本号，保持和maven一致
export VERSION="2.1.0-SNAPSHOT"
# 构建
export TEMP_DIR=/tmp/mlsql-engine_${MLSQL_SPARK_VERSION}-${VERSION}
export UPLOAD=false
export ENABLE_CHINESE_ANALYZER=false
./dev/package.sh

# 在tmp目录生成发行包
rm -rf ${TEMP_DIR}
mkdir -p ${TEMP_DIR}/libs
cp streamingpro-mlsql/target/streamingpro-mlsql-spark_${MLSQL_SPARK_VERSION}_${SCALA_VERSION}-${VERSION}.jar ${TEMP_DIR}/libs

if [[ "${ENABLE_CHINESE_ANALYZER}" == "true" ]]; then
  echo "cp -r lib/*.jar ${TEMP_DIR}/libs/"
  cp -r lib/*.jar ${TEMP_DIR}/libs/
fi

## 生成启动脚本等
cp dev/start-local.sh ${TEMP_DIR}

cat << EOF > "${TEMP_DIR}/start-default.sh"
if [[ -z "\${SPARK_HOME}" ]]; then
    echo "===SPARK_HOME is required==="
    exit 1
fi

SELF=\$(cd \$(dirname \$0) && pwd)
cd \$SELF

./start-local.sh
EOF

cat << EOF > "${TEMP_DIR}/README.md"
1. Configure env SPARK_HOME
2. Run ./start-default.sh
EOF

chmod u+x  ${TEMP_DIR}/*.sh

cd ${TEMP_DIR} && cd ..
tar czvf mlsql-engine_${MLSQL_SPARK_VERSION}-${VERSION}.tar.gz mlsql-engine_${MLSQL_SPARK_VERSION}-${VERSION}
```