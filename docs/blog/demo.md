# MLSQL Stack体验站点火爆来袭

MLSQL的体验站点终于来了，点这里 [MLSQL Console](http://jielongping.com:65092).

## 如何使用

MLSQL Console 现在是自助的 注册和登录的。首先第一次先点击注册啦：

![](http://docs.mlsql.tech/upload_images/267f7d5b-58a2-4d26-9bc1-438b7f034ece.png)

注册完成后直接就进去了。

进去后，我们提供了示例数据：

![](http://docs.mlsql.tech/upload_images/8ed069a1-0887-4875-b5b1-c61a35b9322e.png)

如果在【分析工坊】利用，直接点击右键，选择打开即可。
如果需要在【控制台】使用，黏贴复制运行如下命令即可：

```sql
load delta.`public.example_data` as cc;
```

Console的使用文档看这里：[MLSQL Console 官方文档](http://docs.mlsql.tech/mlsql-console/)

## 关于资源

这次我们Demo站点提供了 16核，14G内存。 然后使用的是 mlsql-engine_2.4-2.0.1， 也就是基于Spark 2.4.3的版本。 后续我们会根据情况调整资源大小。后续我们也会尝试开启文件上传，让用户可以处理自己的感兴趣的数据。

 