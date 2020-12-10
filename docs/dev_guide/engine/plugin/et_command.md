# 命令行开发

在上一篇文章里，我们开发了一个没啥用的EmptyTable,正常使用是这样的：

```sql
-- table1 就是train的第一个参数df
run table1 
as EmptyTable.`` 
-- where 在train方法里可以通过params拿到
where ... 
-- outputTable 就是train的返回值
as outputTable;
```

如果我希望像下面这么使用怎么办？

```shell
!emptyTable _ -i table1 -o outputTable;
```

在EmptyApp里加上一句话即可：

```
package tech.mlsql.plugins.ets

import tech.mlsql.dsl.CommandCollection
import tech.mlsql.ets.register.ETRegister
import tech.mlsql.version.VersionCompatibility

/**
 * 6/8/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class EmptyTableApp extends tech.mlsql.app.App with VersionCompatibility {
  override def run(args: Seq[String]): Unit = {
    //注册ET组件
    ETRegister.register("EmptyTable", classOf[EmptyTable].getName)   
    }
    //注册命令,注意，语句最后没有分号
    CommandCollection.refreshCommandMapping(Map("saveFile" ->
      """
        |run ${i} as EmptyTable.`` as ${o}"
        |""".stripMargin))


  override def supportedVersions: Seq[String] = Seq("1.5.0-SNAPSHOT", "1.5.0", "1.6.0-SNAPSHOT", "1.6.0")
}


object EmptyTableApp {

}

```

这个时候，你既可以用run语法，也可以用命令行了。MLSQL还支持比较复杂的脚本化方式。前面的例子是使用命名参数，用户也可以使用占位符：

```
run {0} as EmptyTable.`` as {-1:next(named,uuid())}"
```

这个时候语法是这样的：

```
!emptyTable table1 named outputTable;
```

其中`{0}`表示第一个参数。 {-1}表示不使用占位，而是使用匹配，匹配规则是`named`字符串后面的值，在这里是outputTable,如果没有则使用uuid()函数随机生成一个。

如果不想事先确定用户会填写什么参数，可以这么写：

```
run command as EmptyTable.`` where parameters='''{:all}"
```

不过需要修改下获取参数的代码，你可以在EmptyTable插件中通过如下方式获得一个字符串数组：

```
val command = JSONTool.parseJson[List[String]](params("parameters"))
```
然后自己匹配命令。

