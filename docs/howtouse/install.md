# MLSQL Console 安装

## 下载

下载地址： [2.0.0](http://download.mlsql.tech/2.0.0/)

MLSQL Engine 和 MLSQL Console需要配套。MLSQL Engine的安装，请参考[MLSQL Stack 安装介绍中的 MLSQL Engine部分](http://docs.mlsql.tech/zh/installation/downloa_prebuild_package.html)

下载解压后，会看到一个mlsql-console-2.0.0文件夹，进入该目录：

![](http://docs.mlsql.tech/upload_images/102b029c-19a0-423e-8776-e3128ca8eb4c.png)

### Step1 数据库配置
首先创建数据库，假设你数据库名称为 mlsql-console， 使用该目录下的 console-db.sql 文件进行表创建。

修改application.docker.yml中标记有 MYSQL_HOST 字样的部分，修改连接地址，账号密码。

### Step2 修改start-default.sh

在start-default.sh中有个MY_URL，请将127.0.0.1转换成你的实际机器IP（内网）。如果你要修改端口，你需要同时修改application.docker.yml以及start-default.sh 中的MY_URL的端口。


### Step3 启动服务

现在可以运行 ./start-default.sh 启动MLSQL Console了。默认端口为9002.

> 恭喜，你已经完成安装部分。请访问 http://127.0.0.1:9002 完成剩下配置。我们在下一个章节会告诉你如何配置。



