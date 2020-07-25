# 简单使用示例

我们会通过一个简单csv格式数据处理来介绍Analysis Workshop的功能。

### 上传数据

假设用户有一个在本地的CSV文件，我们通过 Console->Tools/Dashboard -> Upload 进行上传：

![](http://docs.mlsql.tech/upload_images/2e555f19-73c6-42d3-9b05-4dde279698c9.png)

将文件拖拽到上传框后，就可以了。

![](http://docs.mlsql.tech/upload_images/aa4b40cc-3e5b-44af-ac87-99b98e1a1b7d.png)

系统提示你上传成功，你可以通过命令行查看。不过我们可以到直接进入 Analysis Workshop -> FileSystem 查看：

![](http://docs.mlsql.tech/upload_images/5675aed0-7759-4bd5-ba47-9a4246a79bfb.png)

可以看到，我们上传的文件已经在 /tmp/upload里了。

### 打开文件

右键单击文件，会弹出一个配置框：

![](http://docs.mlsql.tech/upload_images/1fcb0d50-fc2f-4bd2-9659-b053d5bbd0a4.png)

他会询问打开的类型以及需要配置的参数。我们只需要配置header等于true,这表示我们会将csv的第一行作为字段名称。

其他的无需配置，点击Ok，此时 **数据操作台**，**数据展示台** 会出现：

![](http://docs.mlsql.tech/upload_images/851d02bf-e1e5-4258-a4f7-56c3b021d174.png)

### 查看文件记录条数

我们可以在**数据展示台**里点击下拉框，

![](http://docs.mlsql.tech/upload_images/e76417df-bc6c-4106-9116-68342b171fa9.png)

然后点击count即可：

![](http://docs.mlsql.tech/upload_images/6d54654e-ea42-4934-aef2-e7a09b6083bd.png)

此时我们看到，CSV文件里有接近7万条数据。

如果希望回到前面的状态里，只需要点击操作台里的 【Rollback】 按钮即可。 它会撤销掉刚才的count操作。

![](http://docs.mlsql.tech/upload_images/27805032-cb9a-4259-9ac0-783eb067e858.png)


### 过滤产品名

如果我们想过滤 Product Name 为 Consumer Loan 数据单独看看，可以点击 **数据展示台**的搜索标记：

![](http://docs.mlsql.tech/upload_images/214c7943-7beb-4641-9e2d-46f7929da00d.png)

点击运行后：

![](http://docs.mlsql.tech/upload_images/214e7db1-111b-4e04-b431-d8375e0be8b2.png)

过滤完成后，我们希望把这个结果保存下来，但是保存之前，我们并不需要这么多列，这个时候我们可以选择我们需要的列，并且修改列名。

### 选择我们需要的列

![](http://docs.mlsql.tech/upload_images/f2fdd8e5-e195-4760-9e91-87f7307842c6.png)

点击上面的 【Apply】 按钮，他会将你的选择实时反馈出来。当然，如果你选择错误，你可以使用 【Rollback】进行返回。

### 修改列名

因为带有空格的列无法保存到Table Workshop里，所以我们先来修改个名字：

![](http://docs.mlsql.tech/upload_images/0fe1c219-6f3b-41ed-8718-f067d0987955.png)

点击Apply之后，你会发现原来的列还被保留：

![](http://docs.mlsql.tech/upload_images/ed4c630e-c80b-428d-bb8f-b6e6bf3443bd.png)

我们再去选择一次即可。

### 保存我们的工作成果

点击 【Save As】 按钮后，会弹出如下框：

![](http://docs.mlsql.tech/upload_images/87fe0326-1752-4667-98f7-ca0d5fd881d5.png)

我们取名为 table1 并且，我们希望能够把数据持久化下来，那么可以勾选 Persit Table.

点击Ok之后，就可以**立刻**在 Table Workshop里看到它：

![](http://docs.mlsql.tech/upload_images/118731b3-a121-4a7d-8eef-f72a85b75658.png)


因为我们对它进行了持久化，所以根据数据大小，我们在适当的时候可以在FileSystem里看到它：

![](http://docs.mlsql.tech/upload_images/8da600c2-94ba-41bf-a69f-24cafed2d9ff.png)

然而，能不能在FileSystem中看到，并不影响你对它的使用。

### 重新打开原来的csv文件并且关联table1

我们按前面提到的方式，重新打开csv文件。接着，我们希望和和已经处理的table1进行关联。点击Join标签，会出现一个向导：

![](http://docs.mlsql.tech/upload_images/7880e9f9-c3ea-434c-970e-eac5008c8fb7.png)


在下拉框中，你可以看到刚才我们保存的table1表。接着我们根据DataReceived字段关联：

![](http://docs.mlsql.tech/upload_images/bf115eec-a34d-4a63-8b84-9291c031c19f.png)

最后一步，我们选择两个表里需要的字段：

![](http://docs.mlsql.tech/upload_images/879aed88-388a-402e-b752-ad7cdee7f5e3.png)

执行Apply:

![](http://docs.mlsql.tech/upload_images/d4e6fb97-2846-4c7f-a4fc-2f968b154c03.png)

可以看到效果了。如果需要，我们可以继续保存这个结果为table2.

![](http://docs.mlsql.tech/upload_images/d845470f-3b43-4bdb-97ca-fa23e00c8d81.png)


## 打开 Table Workshop里的表

方式是相同的，右键单击表名即可。我们这里打开 table1.

![](http://docs.mlsql.tech/upload_images/8c95dffc-7edb-4806-ab4e-d72a61ff683f.png)

## 统计SubProduct不同种类的个数

打开table1后，点击 Agg标签对 SubProduct 的groupBy进行勾选

![](http://docs.mlsql.tech/upload_images/c9645632-c463-4a1b-ad2d-80653ffc8af8.png)

点击 【Choose Function】, 输入count（会自动提示）。然后在New FieldName 里填写num,点击ok即可。 如果你对函数熟悉，你也可以直接在Mannual Transform里自己填写。

![](http://docs.mlsql.tech/upload_images/6125a0b6-0989-43e5-8855-98df86b5da26.png)

现在点击 Apply:

![](http://docs.mlsql.tech/upload_images/90afb0bb-e787-474b-aac0-2c931b328fab.png)

如果SubProduct 种类特别多，你只想看其中某个产品的数目，你可以使用前面提到的搜索功能，直接在**展示控制台**里查看，也可以点击filter标签，填写你要的产品名称，：

![](http://docs.mlsql.tech/upload_images/422ce253-a25a-4730-a156-6d54f9276520.png)

点击Apply: 

![](http://docs.mlsql.tech/upload_images/8586b665-dcd4-4abb-9505-0210b09a9b4e.png)

如果你发现过滤错了，你可以点击 【Rollback】 撤回。

对于任何结果你都可以使用【Save As】进行保存。

Filter的控制能力很强，你可以任意对条件使用嵌套的【且】和【或】组合进行过滤。

### 小结

从上面我们可以看到，使用Analysis Workshop已经可以实现非常复杂的数据处理了。





