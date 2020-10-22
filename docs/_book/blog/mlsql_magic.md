#  神奇语言MLSQL

> MLSQL [线上体验地址](http://docs.mlsql.tech/mlsql-console/blog/demo.html)

下面是官网对MLSQL的定义：

![](http://docs.mlsql.tech/upload_images/c0624853-de6f-4da5-9a66-ebcc91bd0d0d.png)

一个面向大数据和AI的语言。不过今天，我们纯粹从语法角度出发，然后进一步谈谈MLSQL的设计理念。

> 关于MLSQL语法，我之前也写过两篇文章【[SQL复用告别拷贝黏贴！兄dei， 来看看](https://zhuanlan.zhihu.com/p/138405931)】,【[SQL一秒变命令，分析师都爱哭了](https://zhuanlan.zhihu.com/p/138475580) 】比较正经，大家也可以看看。

## 初看语法

首先，看下面这句：

```shell
!hdfs -ls /;
```

这竟然是一句合法的MLSQL语句。运行结果如下：

![](http://docs.mlsql.tech/upload_images/32d8f571-77e9-41ca-8f9f-b03855d545a4.png)

接着来看：

```
!tableRepartition _ -i newcc_temp -num 2 -o newcc;
```

这个语句在MLSQL中也是合法的。

再接着看：

```sql
select * from rootDir 
as output;
```

依然是一条标准的MLSQL语句。

别惊讶，还有：

```python
!ray on newcc '''
import ray
from pyjava.api.mlsql import RayContext
ray_context = RayContext.connect(globals(),None)

def echo(rows):
    for row in rows:
      row1 = {}
      row1["ProductName"]="jackm"
      row1["SubProduct"] = row["SubProduct"]     
      yield row1
          
ray_context.map_iter(echo)

''' named mlsql_temp_table2;
```

这个句子是不是有点惊悚。

> 哎呀感觉语法乱糟糟，其实没有啦，MLSQL真实语法只有[八条句式](http://docs.mlsql.tech/zh/grammar/),上面看着不像SQL的都是语法糖。

不过在上面我们看到了三个东西：

1. 命令行语法
2. SQL语法
3. Python语法

上面的提到的 `!hdfs`,`!tableRepartition `,`!ray` 其实最终都会被转化为下面的语法：

```sql
run command as 【插件名称】.`` where 条件
```

Python的支持也是通过插件完成的，也就是 `!ray`。

## 为什么要这么设计

我们认为可以用命令行解决的数据分析问题，就用命令行，命令行不够，那么用SQL，SQL还不够，那么用Python,实际上，在MLSQL中我们可以混合使用这三者，从而高效的解决问题。 

数据分析和传统编程语言不同，我们希望最高效的进行数据探索。比如，我希望看看我当前目录有什么数据，如果使用编程语言，比如python,你可能还要去找个python如何遍历文件的demo. 而如果使用MLSQL,很直接：

```
!hdfs -ls /;
```

和hadoop命令一毛一样。这意味着你可以直接在web终端上用一行超级容易记住的指令查看自己主目录下的数据。

而如果你看到了一个文件，想加载然后简单筛选查看，在MLSQL中是这样的：

```
load csv.`/tmp/example.csv` where header="true"
as exampleTable;

select a,b,c from exmapleTable 
as output;
```

我们通过load加载了csv文件，加载的条件是header="true",最后加载的文件被映射成表，之后在下一个SQL语句中被使用。 对于数据探索来说，这就非常直观了。

SQL能够作为当今数据分析的主流语言，核心在于它的学习门槛足够低，是个申明式语言。传统通用语言，他什么都可以干，但是干什么都需要一些通用语言的基础： 变量，scope,引用，分支嵌套循环结构，各种集合类，面向函数，面向方法。当然了，还有各种第三方库，环境等等。你想较为熟练的去解决一个问题，大体没有一到两年是不行的。

另外，从另一个角度思考，用Python之类的主流语言，你相当于在学习一个可以解决大部分你不需要解决的问题的东西，这相当于为了解决通行问题，你拿到了零部件，而不是一辆能跑的汽车，你花了大量时间去组装汽车，而不是去操控汽车。大部分非计算机专业的同学核心要解决的是数据操作问题。

比如Excel（以及类似的软件）为什么这么普及，只要接触过电脑的人，大体都会，原因是因为这是有动力驱动的。无论你是摆地摊，开餐馆，或者在办公室做个小职员，在政府机构做工作，你都需要基本的数据处理能力，本质上是信息处理能力。这个是每个人必要的。 但是Excel有Excel的限制，譬如你各种点点点，还是有点低效的，有很多较为复杂的逻辑也不太好做。什么交互最快？语言。你和计算机系统约定好的一个语言，有了语言交流，总是比鞋子写文章更高效的。这个语言是啥呢？就是SQL。

然而SQL一开始是为数据库查询而设计的语言，如果完全沿用SQL，其实会带来很多难题。MLSQL通过扩展SQL，譬如引入命令行语法糖，支持变量等等使得他更加易用，并且保持足够的简单。

但是作为一门通用的解决大数据和AI问题的语言，他又需要有足够的灵活性，所以我们引入了大家最熟悉的Python,用户可以将Python代码内嵌进SQL中从而实现对SQL表中数据进行处理。这会带来很多便利，比如我开发了一个[脚本插件 save_excel](http://docs.mlsql.tech/mlsql-console/store/save_excel.html),这个插件是内嵌了Python的Pandas完成excel的生成的功能，通过原生的SQL语言，你是很难做到的。

通过融合命令行，SQL，Python,然后以表（数据）为纽带，联通这些语法，保证用户可以高效的对数据进行分析和加工，这是MLSQL设计的核心理念。

懂编程的人为不懂编程的人提供更好的工具，这个在MLSQL中得到了充分的体现，你可以用Java/Scala/Python等语言给MLSQL开发各种插件，这些插件会以命令行或者SQL的形态交付出去，从而让只会命令行或者SQL的同学可以更加高效的解决问题。 对于只会SQL的人，他也可以将自己的SQL包装成命令行去给其他同学使用，甚至是可以反向的给会编程的同学，因为这段SQL其实包含的是更多的业务知识。

另外，通过对Web Console进行精心设计，以及基于MLSQL的优秀能力，我们还提供了无码数据分析平台，[analysis-workshop](http://docs.mlsql.tech/mlsql-console/analysis-workshop/)

## 最后

实际上，MLSQL虽然是一门语言，相比传统的语言，他的门槛是足够低的，让分析师，算法，研发，设置是产品经理，运营都可以掌握。 通过MLSQL,大部分信息产业从业者都可以更好的玩转他们的数据。 欢迎体验：[线上体验地址](http://docs.mlsql.tech/mlsql-console/blog/demo.html)






