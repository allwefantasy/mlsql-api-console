#脚本获取接口

> 1.7.0-SNAPSHOT/1.7.0 及以上版本可用

MLSQL Console本身提供了脚本管理功能，你可以书写，调试以及运行脚本。 在一些特定场景中，我们需要其他程序能够访问这些脚本，然后将这些脚本发送给引擎执行。典型场景如调度服务。

## 接口1：

```
http://[mlsql-console-url]/api_v1/script_file/get
```

参数：


| 参数名 | 类型 | 含义   |
|-----|----|------|
| owner  | 字符串 | 拥有脚本的用户名 |
| id  | 数字 | 脚本id |

因为安全方面的原因，你还需要在header里传递`access-token`. access-token 对应的值在 MLSQL Console的application.yml配置文件里。 这个token具有很高的权限，请勿泄露。

![](http://docs.mlsql.tech/upload_images/bd9f05c2-b254-435b-b7e3-ced3c562fd49.png)

## 接口2：

```
http://[mlsql-console-url]/api_v1/script_file/include
```

参数：


| 参数名 | 类型 | 含义   |
|-----|----|------|
| owner  | 字符串 | 拥有脚本的用户名 |
| path  | 脚本路径 |  |

同样，因为安全方面的原因，你还需要在请求header里传递`access-token`. access-token 对应的值在 MLSQL Console的application.yml配置文件里。

![](http://docs.mlsql.tech/upload_images/bd9f05c2-b254-435b-b7e3-ced3c562fd49.png)

MLSQL Console内部include语法内容获取，也是基于这个接口完成的。

![](http://docs.mlsql.tech/upload_images/9b8ef6af-ced9-4d2a-8035-ad140257f3eb.png)









