# 轻松分析阿里云OSS数据

MLSQL Engine 终于原生支持阿里云了。你现在只要花几分钟就能用MLSQL（或者分析工坊） 来对OSS上的数据分析。与之配套的是，我们开通了`analysis.mlsql.tech`域名,用户可以在这里创建一个基于阿里云的Engine. 


具体原理是，用户提供一个具有在阿里云创建ASK的AccessKey，MLSQL Console会使用该AccessKey在用户的账号下创建对应的MLSQL Engine，然后返回相应的访问地址和随机Token,用户在MLSQL Console 注册下这个地址和Token就能使用。

> 为了保证安全以及使用上的便利，我们建议用户新生成一个权限较大的AccessKey,然后创建完成后，就可以删除掉。

## 在阿里云上创建云引擎

注册登录后，进入设置页面：

![](http://docs.mlsql.tech/upload_images/566b085d-1ba6-483a-8a74-60665edfe6b2.png)

在左侧菜单栏选择`创建云引擎`:

![](http://docs.mlsql.tech/upload_images/446cbb1d-5837-436e-9a02-4de861bfe94a.png)

填写相关信息：

![](http://docs.mlsql.tech/upload_images/3e9c51a5-3bf9-40bd-9dd0-049fe1ab80fb.png)

> OSS AccessKey是必须一直有效的，因为MLSQL Engine会将其作为底层分布式存储。
> 但AccessKey一旦集群创建完毕，就可以删除。避免发生泄露后造成损失。

点击确认后，就会进入一个日志界面，前十秒可能没有数据，大家稍微耐心，日志会自动刷新。途中红线框部分标记我们成功创建了一个Engine：

![](http://docs.mlsql.tech/upload_images/c523a0e0-ad42-4791-9669-ea97d761245a.png)

同时，在你的阿里云`容器服务 - Kubernetes`控制台上也可以看到我们新创建的ASK集群，点击进去可以看到非常详细的MLSQL Engine相关的信息：

![](http://docs.mlsql.tech/upload_images/9118cfed-69c8-4671-89ac-de044b785084.png)

通常，你获得连接信息后，根据集群规模大小，可能需要等待一分到10分钟不等才能真正开始试用MLSQL Engine,原因是大量的容器创建需要一定的时间。用户可以在阿里云中看到更详细的情况。

![](http://docs.mlsql.tech/upload_images/97336d49-e042-436b-9586-cf2a9f9a58ce.png)

当所有容器都处于running状态了，就ok了，如下图：

![](http://docs.mlsql.tech/upload_images/3d32a9d1-5468-46db-a53d-035d3493d56c.png)


## 注册新创建的云引擎到MLSQL Console里
拿到上面的信息，就可以注册引擎信息到MLSQL Console了：

![](http://docs.mlsql.tech/upload_images/0c4dd15f-18b4-4f0f-9cf7-9ae5f4a32ddf.png)

最后，将其设置为自己的默认引擎：
![](http://docs.mlsql.tech/upload_images/5291abc6-3880-4b0a-a676-adcd30ccd74a.png)

如果忘记了，也可以到这里查看：
![](http://docs.mlsql.tech/upload_images/e9cef92f-c040-4fff-9905-b75a39dd2756.png)

## 删除云引擎

如果你不需要使用了，可以删除引擎：

![](http://docs.mlsql.tech/upload_images/f49a9135-2bb8-49d1-91e7-aba633f7c298.png)

![](http://docs.mlsql.tech/upload_images/8a39bed4-0325-49e9-9994-764c7c19cd13.png)

如果删除失败，可以主动到阿里云`容器服务 - Kubernetes`控制台上进行删除。





