# 用MLSQL为Analysis Workshop开发插件

## 前言
我们知道 【Analysis Workshop】 的目标是无SQL化，但最终可以做到等价于任意复杂度SQL。尽管它现在理论上
应该非常接近这个目标了，但是我们认为我们可以通过插件，进一步简化操作，让一个可能需要很多步骤的东西，变成可能只需要
一个步骤就可以完成。

依托于面向大数据和AI语言MLSQL强大的能力，我们可以很轻松的为【Analysis Workshop】添加插件。


## 示例插件：模糊过滤插件

在这篇文章里，我们会开发一个非常简单的插件，该插件可以让用户按如下步骤完成模糊过滤功能：

1. 从当前表中选择需要过滤的字段
2. 填写过滤值
3. 点击Apply运行插件代码，得到新结果集

插件构建的用户界面如下：

![](http://docs.mlsql.tech/upload_images/e7733be5-212a-49af-80a2-b13479f927e1.png)

用户选择 【模糊匹配过滤插件.mlsql】,然后就会出现一个表单，按要求填写表单即可完成对应字段的过滤。

![](http://docs.mlsql.tech/upload_images/798a05aa-90e1-4f28-953b-c2d5d9486684.png)

这个插件的功能等价于字段检索功能：

![](http://docs.mlsql.tech/upload_images/9960ec64-1796-4090-9deb-a7e682b59131.png)

## 开发流程之表单构建

首先开发者需要到 【Console】中新建一个脚本：

![](http://docs.mlsql.tech/upload_images/6dee67b0-d67b-44f8-9be6-2146b6c761f9.png)

接着就可以开始用MLSQL编写插件了。

首先插件需要一些参数，这些参数会以表单的形式供用户填写。在【模糊过滤插件】中，我们需要用户填写待搜索字段以及对应的搜索值。一个select 控件，一个input控件。在MLSQL中可以按如下方式表达：

```
set field="" where type="defaultParam" 
and formType="select" 
and label="过滤字段" 
and required="true" -- 这个字段是必须的
and optionTable="__current_table_fields__" 
;

set field_value="" where type="defaultParam" 
formType="input" and label="过滤值";
```

其中 
1. field/field_value 为对应的表单空间名称，也是我们的参数名称。
2. type="defaultParam"  表示这个set变量可以被前面的变量覆盖。
3. formType表示控件类型。目前只支持 input/select.
4. label 是显示给用户的表单名称
5. required 指定该字段是否是必须填写

对于select 空间，还有几个特有的配置字段：


| 参数名称      | 参数描述 |   |
|-----------|------|---|
| optionStr | 下拉框的值集合，用分号分割。  |   |
| optionTable | 你可以将下拉框的值提前写到一个hive表或者delta表里，然后在这里指定表名。注意表必须包含name,value两个字段。  |   |
| optionSql | 你可以写一条简单的SQL，同样最后结果里必须包含name,value字段  |   |
| selectMode | select空间是单选还是多选。 multiple/tags|   |

optionTable 有一个内置的表，叫`__current_table_fields__`,可以获取当前显示数据的schema.

## 开发流程之数据处理逻辑

在前面，我们用set语法构建了表单，现在，我们可以开始处理数据了。在当前插件中，处理的逻辑比较简单，只用到了select语法，没有用到ET组件等更强大的功能。

```
select * from `${__current_table__}` where `${field}` like "%${field_value}%" as `${__new_table__}`;
```

这里有两个内置变量：

1. \_\_current_table\_\_  当前用户正在操作的数据的名称。
2. \_\_new_table\_\_ 经过插件处理完成后数据的名称

field/field_value 则是前面我们自己设置的两个变量。

## 开发流程之调试

你可以在自己的脚本前面添加

```
set __current_table__ = "***" where type="defaultParam";
set __new_table__ = "****" where type="defaultParam";
```

这样就可以直接在脚本里调试运行了。

## 开发流程之发布

最后调试没啥问题了，就可以发布了：

![](http://docs.mlsql.tech/upload_images/912d3b16-7e66-4dee-835a-194ff1750963.png)

点击Publish即可。现在就可以进入【Analysis Workshop】里使用了。


## 总结

【Analysis Workshop】 插件开发基本分成两步：

1. 构建表单
2. 书写业务逻辑

因为MLSQL支持内嵌Python，以及通过Scala/Java 开发UDF/UDAF等，所以可以实现非常复杂的需求，从而定制化出一些在公司内部非常有价值的插件。







