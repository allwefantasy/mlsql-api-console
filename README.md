# MLSQL Console

MLSQL Console 主要展示了[MLSQL](https://github.com/allwefantasy/mlsql) 的能力，对接方式。

## 本地开发环境设置

软件要求：

1. JDK 8+
2. Maven 3.3+
3. Git
4. MySQL 5.7

推荐Idea Intellj 使用。

配置文件配置：

将 config/application.docker.yml 改成  config/application.yml.

将如下内容修改为你自己的数据库连接信息：

```
#mode
mode:
  development
#mode=production

###############datasource config##################
#mysql,mongodb,redis等数据源配置方式
development:
  datasources:
    mysql:
      host: MYSQL_HOST
      port: 3306
      database: mlsql_console
      username: xxxxx
      password: xxxxx
      disable: false
      initialSize: 3
      removeAbandoned: true
      testWhileIdle: true
      removeAbandonedTimeout: 30
      filters: stat,log4j
      maxWait: 100
```

启动类：

```
tech.mlsql.MLSQLConsole
```

右键启动即可。

SQL脚本(找最新的就好)：

1. mlsql_console_2020-12-24.sql






## 安装部署

参考官方文档[MLSQL Console安装](http://docs.mlsql.tech/mlsql-stack/howtouse/install.html)






