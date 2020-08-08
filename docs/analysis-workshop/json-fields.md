# Json字段展开

实际业务场景中，我们很多字段会是Json文本。AnalysisWorkshop 提供了两种方式供用户使用。一种是需要学习函数，一种则不需要。

### 无需函数展开Json字段

在AnalysisWorkshop里打开表，然后进入【操作区】的 【字段】/【Json字段展开】栏目，选择包含json格式的字段：

![](http://docs.mlsql.tech/upload_images/aba838b3-a5ae-4e04-943b-86ae92b78034.png)

勾选后，会出现一个 添加 符号：

![](http://docs.mlsql.tech/upload_images/04c7b3f5-48b1-4c66-b949-4af1027b3261.png)


点击后，系统会罗列出json字段里所有的路径。

![](http://docs.mlsql.tech/upload_images/41c0d987-17b2-4f33-bed0-a449953f867a.png)


假设我们选择 web_paltform字段，然后取一个新的名字,点击Apply，这个时候你操作的数据就多了一列 web_platform了：

![](http://docs.mlsql.tech/upload_images/e49c67f2-9de2-47cd-869e-bd3735eccdf2.png)

## 使用函数

用户也可以使用get_json_object函数，进入【操作区】的 【字段】/【转换字段】栏目，填写函数和字段名称：

![](http://docs.mlsql.tech/upload_images/c82486c4-27e3-4a4c-b26f-5aa2d925f50e.png)

点击Apply后也能得到和前面相同的效果。不过这里需要用户掌握get_json_object函数的用法以及XPath的使用规则。



