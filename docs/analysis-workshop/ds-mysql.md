# MySQL数据库配置
   
【Analysis Workshop】 支持用户自己添加MySQL数据库，从而能够对数据库里的数据进行。使用【Analysis Workshop】对MySQL分析最大的优势是，可以和其他存储器的数据做关联分析，比如数仓，数据湖，文件系统等。
   
   本文将教会读者配置MySQL数据库，以及一些简单的分析用法。
   
### 配置
   
   登录进 MLSQL Console 后，进入 设置 -> 数据源 -> 连接MySQL:
   
   ![](http://docs.mlsql.tech/upload_images/484df4ad-ab1c-4c93-a93c-886eacaf6a3f.png)

之后填写MySQL信息，点击应用。系统提示成功后，就可以在 【MySQL列表】里看到刚才的配置：

![](http://docs.mlsql.tech/upload_images/4b712597-e896-4793-941a-a99a762b9065.png)

> Note: 别名如果没有配置，默认和数据库名是一致的。

进入 【Analysis Workshop】，就可以看到【MySQL】标签下，多了一个数据库：

![](http://docs.mlsql.tech/upload_images/65f9d36d-13c8-45c3-9aa9-c8ebd3fad8c9.png)

### 使用

双击MySQL标签下的任何一张表，系统会弹出一个打开选项，此时会出现两种情况：

1. 可以配置参数加速表的打开
2. 不可以配置参数，只能单线程打开表

两种情况哪个会出现的依据是表里面是否有数字类型的字段。【Analysis Workshop】 为了能够多线程的去拉取MySQL数据，需要按数字列按区段对数据进行切割。

下面是不能使用的提示：

![](http://docs.mlsql.tech/upload_images/de773c66-365d-40f1-b7b4-1f5eddad7cda.png)

这个时候直接点击确认，就可以看到数据了：

![](http://docs.mlsql.tech/upload_images/aaed6d5c-de6b-408f-9e2b-0f8b5975bbec.png)

如果能使用的话，用户需要选择下切割字段：

![](http://docs.mlsql.tech/upload_images/d47e4fb8-4eb5-4214-ab50-1fcb057ef3d3.png)

我这里选择 id字段（自增id字段是最佳的切割字段），系统会自动补全最大值，最小值，

![](http://docs.mlsql.tech/upload_images/93838ac2-ea3b-4246-b838-4b543c4a2583.png)

然后就是分区数，默认是10。你也可以调整。 切分方式就比较简单的，最大值减去最小值，然后除以10，就得到区间的id范围，这样可以并行的去MySQL获取数据。

如果系统自动填充超时（可能MySQL表很慢），用户自己手动填写下就好。

之后点击确认就可以继续对数据做分析了。

### 如何将将MySQL表数据导出到文件系统，数据湖或者数仓里？

可以点击[Save As]按钮，将表保存到【暂存区】，

![](http://docs.mlsql.tech/upload_images/7187ddd6-7bf1-48c7-8caa-2e986d7d9d86.png)

然后利用暂存区的导出功能导出到其他数据源里：

![](http://docs.mlsql.tech/upload_images/c4dba086-5303-47ba-af40-31380801d621.png)

稍后你就可以在这里看到数据了：

![](http://docs.mlsql.tech/upload_images/4d566076-2c1d-428e-8fa3-7a9baacc5845.png)

如果你一直没看到数据，可以进入【控制台】 的 Tools/Dashboard/History里看进度：

![](http://docs.mlsql.tech/upload_images/b80df27d-9e91-4e97-9f70-9238aa8bfcfc.png)



