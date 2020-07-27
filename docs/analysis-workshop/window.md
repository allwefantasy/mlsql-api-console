#使用窗口进行数据分析

窗口(Window)函数是SQL中的术语，亦是高阶分析必须的。尽管在Analysis Workshop中我们会以向导的方式帮住你去使用，但是主动了解Window窗口函数必要的基础知识依然是有必要的。

传统我们可以对数据进行分组聚合亦或是简单的对字段进行操作。但是很多场景，我们希望对每条数据的前后N条数据（或者特定区间）的数据进行某种计算，这就像用一个框每次滑动一条记录，每次计算的时候计算整个窗口包含的数据。

在Analysis Workshop中，我们需要四步完成一个或者多个窗口一致的统计。

1. 对数据分组（这是性能上的要求）
2. 对每个分组的数据进行排序（不是必须的）
3. 设置窗口大小（不是必须的）
4. 应用窗口计算函数


## 准备数据

我们需要准备一些测试数据，这里直接使用插件完成数据的准备。首先进入 Console，输入如下代码：

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

## 窗口分析

在Analysis Workshop中打开 example.simpleData表，然后进入操作台 【Window】 选显卡，

![](http://docs.mlsql.tech/upload_images/013a3589-2ed5-4be7-a84c-c590307f872f.png)

第一步选择分组字段，可以是多个，我们选择部门：

![](http://docs.mlsql.tech/upload_images/58a83a00-ee7a-4068-8026-f400666517c8.png)

接着按薪水排序：

![](http://docs.mlsql.tech/upload_images/d0f35b1d-cf39-4503-a8b7-807d8bb84d25.png)

选择窗口大小，Preceding和Following 都不填写的话，那么默认Preceding 是unbouded, Following是current row. 正常大家填写数字即可。
：

![](http://docs.mlsql.tech/upload_images/30555c69-ec15-4c48-ae0f-f7acb816d75b.png)

我们对窗口计算两个值，一个是部门薪水总和，一个是雇员薪水排名。
先看如何计算薪水综合：

![](http://docs.mlsql.tech/upload_images/42f937a3-6114-4eee-aff8-2cb0e6ce9ed9.png)

填写完成后，点击【Add】 按钮，下侧会显示添加结果：

![](http://docs.mlsql.tech/upload_images/76db0e5a-3d26-45d5-98a8-10df7112a1b0.png)


在上面搜索框中你可以搜索一个函数的具体用法：

![](http://docs.mlsql.tech/upload_images/44513b15-5e51-4d54-a359-c03792c577b9.png)

接着我们进行排名，使用rank函数：

![](http://docs.mlsql.tech/upload_images/7b38f428-9f19-438e-a83b-f112b72b86e5.png)


添加完成后，点击 【Apply】即可看到结果：

![](http://docs.mlsql.tech/upload_images/94e98fba-2aca-432f-a524-f1739a3a00a4.png)


如果需要可视化该图的话，进入【Dash】标签填写必要信息即可：

![](http://docs.mlsql.tech/upload_images/11a40be5-4cdf-4e1a-8b44-94509b02aa35.png)









