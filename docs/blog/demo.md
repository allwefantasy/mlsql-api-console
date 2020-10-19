# MLSQL Stack体验站点火爆来袭

MLSQL的体验站点终于来了，点这里 [MLSQL Console](http://jielongping.com:65092).

另外有Bug或者建议的，可以在[Issues](https://github.com/allwefantasy/mlsql/issues)提问题一起探讨哦。

## 站点最新更新

### 升级到2.1.0-SNAPSHOT,支持分支语句 (2020-10-06)

[MLSQL 支持条件分支语句](http://blog.mlsql.tech/blog/mlsql-ifelse.html)

### Excel 支持 (2020-09-01)
增加excel插件支持，支持在MLSQL中使用python处理excel. 上传你的excel尝试下吧。 [结合Python读取Excel](http://docs.mlsql.tech/mlsql-engine/python/read_excel.html) 

或者使用如下方式加载excel:

```sql
load excel.`/tmp/upload/测试.xlsx` 
where useHeader="true" 
and maxRowsInMemory="100"
as output;
```

保存：

```sql
save overwrite table1 as excel.`/tmp/upload/测试-1.xlsx` 
where useHeader="true";
```

## 感谢服务器资源支持者

这是支持者的公众号，欢迎关注：

![](http://docs.mlsql.tech/upload_images/280f02b6-e715-45f2-aec5-732d7fcce8b0.png)

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

##  我想玩自己的数据怎么办

进入【控制台】，找到如下的组件：

![](http://docs.mlsql.tech/upload_images/a8392542-b4ed-47e1-a73f-c63221cf5b62.png)

拖拽文件上去，然后等待，直到出现这个标志，

![](http://docs.mlsql.tech/upload_images/d9c81e3f-0e7e-4bc5-b051-879f0028adbe.png)

表示上传成功。记得文件不要太大哦，最好10M以内。

接着点击右侧的 FileSystem,就能看到你上传的文件了：

![](http://docs.mlsql.tech/upload_images/6b15cbd1-e682-4e13-86a4-4114c0090379.png)

点击进入【分析工坊】，再点击文件系统,右键即可打开文件：

![](http://docs.mlsql.tech/upload_images/15fe7889-ecd0-48d6-ae6f-3e4c10a67164.png)

结果得到显示：

![](http://docs.mlsql.tech/upload_images/78e4f952-7b5e-4930-bb88-25b702cbef35.png)

尽情玩耍吧。

## 如何下载自己生成的数据

进入【控制台】，展开 【Quick Menu】,双击 【Download file to computer】，具体位置在这里：

![](http://docs.mlsql.tech/upload_images/5f719041-0753-4004-b11f-d54dc350a87e.png)

弹出框如下：

![](http://docs.mlsql.tech/upload_images/0eb69ae4-69b0-4ad3-8991-aa7196f8ee14.png)

填写路径地址：

![](http://docs.mlsql.tech/upload_images/c2c746d8-82c6-4b50-bf35-b116fffd7176.png)

点击Ok，然后Chrome就会进行下载了。

## 关于资源

这次我们Demo站点提供了 16核，14G内存。 然后使用的是 mlsql-engine_2.4-2.0.1， 也就是基于Spark 2.4.3的版本。 


 