#可视化

在Analysis Workshop中，用户也可以选择报表插件绘制报表。下面是制作一张报表的示意图：

![](http://docs.mlsql.tech/upload_images/3193f5e4-643e-4f34-8ce1-2fec99654b8b.png)

报表插件一般使用MLSQL 脚本开发，之后会发布到MLSQL插件商店。

## 准备数据

我们需要准备一些测试数据，我们可以直接使用插件完成数据的准备。首先进入 Console，输入如下代码：

```sql
include store.`tech/mlsql/console/example/SimpleData`;
load delta.`example.simpleData` as output;
```

点击 【Run】，效果如下：

![](http://docs.mlsql.tech/upload_images/ee9cd924-5883-41c7-a0a1-6855051ef8fc.png)

如果你需要自定义存储表，可以设置【targetTableName】,如下示例：

```sql
set targetTableName="example.simpleData2";
include store.`tech/mlsql/console/example/SimpleData`;
load delta.`example.simpleData2` as output;
```

插件【tech/mlsql/console/example/SimpleData】默认会在你的delta的example库下，建立一个simpleData表。

simpleData有三个字段，分别是部门名，雇员名，以及薪水。我们现在的需求是，对按部门分组，统计每个雇员的薪水排名。

## 绘制报表

打开Analysis Workshop,点击右侧DeltaLake标签下的 example.simpleData表：

![](http://docs.mlsql.tech/upload_images/a5e71f03-1a15-442b-bcfd-480724180bdb.png)

进入操作台的 Dash:

![](http://docs.mlsql.tech/upload_images/fffa4ca0-6ef8-47c8-a923-51003f622907.png)

在 【Visualization Plugin】中选择一个合适的插件，点击后右侧【Plugin Parameters】中会出现一些配置参数以及样例图：

![](http://docs.mlsql.tech/upload_images/fa1e7920-14aa-4bca-b645-1d4a3e4a9dff.png)

接着在【Generic】中选择X,Y 字段：

![](http://docs.mlsql.tech/upload_images/d2b9b722-e491-4bdb-a2f3-c96335865c87.png)

最后点击【Apply】可以看到渲染结果：

![](http://docs.mlsql.tech/upload_images/e10e1d51-5130-472a-b3b7-7243ccc7d713.png)

鼠标移动后，可视化区域会有一些菜单选项

![](http://docs.mlsql.tech/upload_images/e0d2709c-9cc9-47f0-8640-562063b68b15.png)

可根据需求使用。

注意： 

> 如果对于效果不满意，需要先点击【Rollback】，再修改配置后点击Apply.








