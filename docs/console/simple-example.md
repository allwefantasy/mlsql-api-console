# 简单使用示例

该章节我们会简单介绍如何通过MLSQL脚本来处理一个CSV文件。

### 上传数据

假设用户有一个在本地的CSV文件，我们通过 Console->Tools/Dashboard -> Upload 进行上传：

![](http://docs.mlsql.tech/upload_images/2e555f19-73c6-42d3-9b05-4dde279698c9.png)

将文件拖拽到上传框后，就可以了。

![](http://docs.mlsql.tech/upload_images/aa4b40cc-3e5b-44af-ac87-99b98e1a1b7d.png)

按照提示，使用 **!hdfs** 命令查看：

![](http://docs.mlsql.tech/upload_images/40ba22cb-612f-4cc4-9aef-d0ba51a5cbba.png)

### 加载数据

MLSQL 使用load加载数据， 书写时系统会自动做一些提示：

![](http://docs.mlsql.tech/upload_images/694f0e62-de88-4c2d-8814-597eacfb0438.png)

书写完成后，就可以点击运行查看了：

![](http://docs.mlsql.tech/upload_images/48437d1a-f771-4e3f-be64-12d4dc5a0b26.png)

### 使用Select语句进行数据处理

MLSQL完全兼容SQL的select语句。我们可以使用Select语句对数据进行处理

![](http://docs.mlsql.tech/upload_images/5402f43d-a9bb-455d-ac13-ad6384de43f3.png)

### 保存数据

MLSQL使用Save语句保存数据。

![](http://docs.mlsql.tech/upload_images/9e38bec8-10ee-4b8a-a435-a768d34961f6.png)

这里把数据保存成了json格式，在FileSytem中也可以看到：

![](http://docs.mlsql.tech/upload_images/cd610a2b-22ad-46d9-bc20-44c5ff23d238.png)


### 注意事项

如果需要保存脚本，需要在右侧新建脚本，然后在对应的脚本里进行保存。








