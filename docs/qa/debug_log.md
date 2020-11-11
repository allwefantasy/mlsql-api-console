# 排查错误，三个系统的日志你都要看

通常第一次安装Console/Engine后，然后欢欢喜喜的执行示例，然后发现Console一直在转圈圈，界面上没有显示具体错误。
这个时候别慌，我们看看怎么排查问题出在哪。而且掌握这篇文章的技巧后，也特别方便在MLSQL群里提问呢，帮助我们更好的帮到您。

在整个过程中，有三个系统的日志可以看：

1. Console日志
2. Engine日志
3. Spark UI以及里面的日志

Console日志是在Console的安装目录里的logs目录下。

Engine的日志是在Engine安装目录下的logs目录下，通常名称是`mlsql_engine.log`。 (如果你发现没有，可以将这个[log4j.properties](https://github.com/allwefantasy/mlsql/blob/master/streamingpro-mlsql/src/main/resources-online/log4j.properties)
拷贝到SPARK_HOME/conf目录里)。

## 说说他们之间的关系

我们可以在Console里配置Engine地址，在配置Engine地址的过程中，还需要配置诸如consoleUrl, fileServerUrl ,authServerUrl等。

 ![](http://docs.mlsql.tech/upload_images/151565d6-1ce3-45fe-8678-3b146a94ad9b.png)


Engine会通过consoleUrl访问Console,比如通知Console任务执行状态等。fileServerUrl ,authServerUrl 则分别和文件上传，权限控制有关。默认都填写
consoleUrl的地址即可，因为Console实现了这些功能的接口。

所以实际上，Console和Engine是要互通的，也就是Engine需要访问Console，Console也需要访问Engine。


