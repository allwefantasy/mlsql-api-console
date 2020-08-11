#脚本执行代理接口

> 1.7.0-SNAPSHOT/1.7.0 及以上版本可用

我们可以通过提交脚本给MLSQL Console,然后Console会提交给后端引擎执行。在提交过程中，我们可以控制后端是异步还是同步返回。

使用MLSQL Console作为代理接口有诸多好处，因为Console会自动填写很多配置，简化了提交的复杂度。

## 接口

```
http://[mlsql-console-url]/api_v1/run/script
```

| 参数名 | 类型 | 含义   |
|-----|----|------|
| owner  | String| 选择执行的用户|
| engineName  | string| 可以选择后端执行的引擎 |
| sql  | string| 待执行脚本 |
| jobName  | string| 任务名称。建议保持不重复，比如使用uuid或者脚本id |
| async  | boolean| 是否异步提交给后端引擎。默认false,需要搭配callback使用 |
| callback  | string| 引擎执行完成后，会通过该URL进行回调|
| sessionPerRequest  | boolean| 默认false,一个用户如果可以并发提交脚本，那么该选项一定要打开|
| access-token  | string| 请求头参数，该token可以在MLSQL Console的配置文件中找到，名字叫： auth_secret|

通过[脚本获取接口](http://docs.mlsql.tech/mlsql-console/developer/script_api.md),然后再通过该接口完成脚本提交，就可以实现调度所需的功能了。

下面是一段示例python代码：

```
import requests
res = requests.post("http://127.0.0.1:9002/api_v1/run/script",
              params={
                  "owner":"allwefantasy@gmail.com",
                  "sql":"select 1 as a as b;",
                  "jobName": "wow"
              },headers={"access-token":"xxxx"})
res.content              
```

结果如下：

![](http://docs.mlsql.tech/upload_images/c52b1731-7ef4-4cd4-8138-aaa09cd54415.png)




