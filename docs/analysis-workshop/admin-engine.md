#Admin管理后台之Engine配置

MLSQL Console后端的执行引擎是 MLSQL Engine.所以我们需要配置了Engine才能真正的数据分析。

MLSQL Console在启动脚本中可以配置Engine相关信息，同时也会配置一些参数高速Engine Console的一些信息，从而实现双方互通。

我们在第一次启动Console的时候，初始化配置里，也会有一个简单的Engine配置，保证配置完成后可以执行最基础的Console功能。如果想更完善的配置，则需要以Admin身份进入Console.

如果你是管理员，那么进入方式界面之后可以看到【管理员选项卡】：

![](http://docs.mlsql.tech/upload_images/086cae65-f306-4ae4-b61c-75b5cd1b85d9.png)

点击进入后再点击左侧Engines选项卡：

![](http://docs.mlsql.tech/upload_images/ad8a75a2-78a2-4704-bb61-3120df5da7b6.png)

打开【编辑模式】即可编辑或者删除已有的Engine配置，或者新增新的Engine配置。

![](http://docs.mlsql.tech/upload_images/cb91da44-fdec-4c7e-9ab1-549bfa99f13f.png)

每个字段的含义：


| 参数 | 说明 | 示例  |
|----|----|---|
|  name  |  引擎名称，需要唯一性   | default这样的字符串 |
|  url  |  引擎地址 | http://127.0.0.1:9003  |
|  home  |  主目录  | /data/mlsql/homes  |
|  consoleUrl  | console的地址。考虑到很多次场景系统自动获得IP地址是错误的，用户需要主动填写，避免Engine无法连接Console   |  http://127.0.0.1:9002  |
|  fileServerUrl  | 文件服务器地址   | 默认填写 consoleUrl |
|  authServerUrl  | 权限校验服务器地址   |  默认填写 consoleUrl |
|  skipAuth  |  是否跳过权限验证  | 建议跳过；如果设置为false表示开启权限验证，Engine会去authServerUrl校验信息  |
|  extraOpts  |  json字段。如果是新增，填写 {}  |   |
|  accessToken  |  Engine如果开启了token验证，需要得到token才能访问  |   |

## Engine配置完成之后还有啥

配置完成之后，你需要在两个地方用到Engine,第一个是在Console写脚本的时候：

![](http://docs.mlsql.tech/upload_images/7ef01f37-6308-4aa7-a5e4-55b83b97146a.png)

这里你可以选择已经配置的Engine去执行你的代码。

第二是 【设置】下面的

![](http://docs.mlsql.tech/upload_images/11f39c2d-1abc-4f0b-a9a5-6f1969226805.png)

给【分析工坊】设置一个默认的执行引擎。

另外，你可能还需要给【分析工坊】设置一个合理的超时时间，取决于你的Engine规模大小以及数据大小：

![](http://docs.mlsql.tech/upload_images/13843be3-ef73-40f9-84cc-725b51141ffc.png)




