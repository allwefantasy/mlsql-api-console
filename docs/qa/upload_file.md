# 文件上传

MLSQL Console文件上传的路口在这里：

![](http://docs.mlsql.tech/upload_images/f726c10e-551d-4fae-ad2c-1cd526260e65.png)

通常文件上传会遇到两个问题：

1. 上传失败
2. 上传成功，但找不到文件

上传失败，可以按如下方式检查：

1. [主目录](http://docs.mlsql.tech/mlsql-console/qa/home.html)是否存在或者Engine有读写权限么？
2. 临时目录大小是否超过配额
3. Engine/Console之间是否确认互通

其中临时目录配额默认是125M，是MLSQL Console用于中转文件的目录。通常用户上传的文件，先要上传到MLSQL Console,然后MLSQL Console再发指令
给Engine,拉取到HDFS目录。

临时目录的文件会保留2个小时。也就是说假设你第一次传了一个120M的文件，接着你再上传需要等两个小时。
你也可以通过修改MLSQL Console启动脚本`start.sh` 来修改配置：

```
java -cp .:${MLSQL_CONSOLE_JAR} tech.mlsql.MLSQLConsole \
-mlsql_engine_url ${MLSQL_ENGINE_URL} \
-my_url ${MY_URL} \
-user_home ${USER_HOME} \
-enable_auth_center ${ENABLE_AUTH_CENTER:-false} \
-single_user_upload_bytes 1073741824 \
-config ${MLSQL_CONSOLE_CONFIG_FILE}

```

通过参数 `single_user_upload_bytes` 我们将用户临时上传目录修改为1G.

如果是上传成功，但是在Console的 FileSystem里没看到，或者通过命令`!hdfs -ls /tmp/upload` 也没有发现文件。这个时候住哟啊是检查下
[主目录](http://docs.mlsql.tech/mlsql-console/qa/home.html) 是不是被创建了。



