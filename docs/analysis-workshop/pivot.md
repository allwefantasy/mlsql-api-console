# 透视表

这可能是最简单的透视表使用了。

假设我们有个产品用户反馈表：

![](http://docs.mlsql.tech/upload_images/5bce3041-d4b0-4be3-ba23-070846267ae9.png)

显然，这个表的记录模式是流水账，记录了某天某个用户对某个产品的一个反馈（Issue）。现在我们想要查看的是，以某天为单位每个产品的反馈数量。

根据分析需求，我们只需要关心时间，产品名称，反馈三个字段。所以第一步我们先做字段筛选，进入【操作区】，选择这三个列：

![](http://docs.mlsql.tech/upload_images/9559a1a1-7440-4c9a-a0d0-54e036799175.png)


点击 【Apply】.

现在，进入透视表菜单：

![](http://docs.mlsql.tech/upload_images/292de33a-0d2f-40b1-b08b-4687641cd1a7.png)


将Date Received 拖拽到左边的【行列】，将 Product Name拖拽到【表头列】，而 Issue 则是作为统计列:

![](http://docs.mlsql.tech/upload_images/fd900f29-27f8-465e-9081-4c818dde97c7.png)

现在选择下统计方法，在当前场景里是选择 count:

![](http://docs.mlsql.tech/upload_images/7979d650-c50f-42d4-992c-e9cd936dfe80.png)

点击 【Apply】:

![](http://docs.mlsql.tech/upload_images/05af11d3-7d53-49cf-83c4-bec3fa4e7f00.png)

这样，就可以看到每天每个产品的Issue情况了。


