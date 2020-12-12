# MLSQL Engine开发环境设置

![](http://docs.mlsql.tech/upload_images/903a4473-d7e0-4461-b0fc-bd89581609b7.png)

![](http://docs.mlsql.tech/upload_images/b0ca2c5e-063a-40ad-b2ab-a66c2fce8b3e.png)



MLSQL主要使用Java/Scala开发，所以我们推荐使用Idea IntelliJ 社区版进行开发。

MLSQL Engine内部的执行引擎是Spark，尽管在设计的时候可以使用其他引擎替换（如Flink），但目前Spark为其唯一实现。

通常而言，MLSQL会支持Spark最近的两个大版本,截止到本文写作时间，我们支持2.4.x,3.0.x。
经过测试的为准确版本号为2.4.3/3.0.0/3.0.1。

因为要同时支持两个版本，但是Spark不同版本的API往往发生变化，我们使用Maven的profile机制切换不同的模块支持。

![](http://docs.mlsql.tech/upload_images/06115eda-65f2-4a50-ac17-df7fb06740d6.png)

红色框选部分，展示了我们对spark 2.3,2.4,3.0进行了适配。实际上2.3已经不再维护了。在未来3.1版本适配完成后，会删除2.3这个适配。

用户可以通过git 下载源码：

```
git clone https://github.com/allwefantasy/mlsql.git .
```

这是一个Maven项目，用户只要是有用idea 按Maven项目打开即可。在后续章节里，我们会详解介绍如何基于Spark 2.4.3 和 3.0.0开发 MLSQL .

