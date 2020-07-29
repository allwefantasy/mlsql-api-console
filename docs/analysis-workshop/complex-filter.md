# 复杂组合条件过滤

Analysis Workshop 引入【组】、【条件】两个基础概念实现复杂的过滤条件组合。

1. 条件， 基本的比较表达式，比如 等于，Like等。
2. 组， 只包含基本条件或者只包含其他组，他们之间只有 【且/and】 或者 【或/or】的关系。

说起来可能比较抽象。我们来看一个具体实例。

在 Analysis Workshop里打开一张表simpleData, 他包含三个字段：

![](http://docs.mlsql.tech/upload_images/44073a57-3606-4256-b3d9-9a6ccb49cbe6.png)

分别是员工，部门和薪资。

现在我们希望过滤出符合如下条件的内容：

1. 部门为 Sales  且 员工薪资大于  1000

2. 部门为Finance且员工薪资小于 3000

1，2的关系为或的关系。

这里，【部门为 Sales】  我们叫做一个基本【条件】，所以第一个规则其实就是一个组，这个组里面有两个条件，分别是部门为 Sales 和员工薪资大于1000。 同理2. 
1，2两个组构成一个新组，我们假设叫 【最终组】，最终组包含这两个组的关系是或。

我们来看看如何使用Analysis Workshop来描述这种关系。

## 构建组

先构建组【部门为 Sales  且 员工薪资大于  1000】，

![](http://docs.mlsql.tech/upload_images/2b9823a2-b502-4d4b-bbcf-4d03d69a0f19.png)

勾选条件，并且点击 【Add Selected conditions to group】 将条件添加到组里。
添加完成后，可以在 Apply Group 标签栏看到新添加的组：

![](http://docs.mlsql.tech/upload_images/3bd8515b-d0df-48c8-af9d-3330342ff5ae.png)

我们可以点击Apply 看下效果：

![](http://docs.mlsql.tech/upload_images/01a086dd-aa19-443f-a9e2-44d63335d2fd.png)

Rollback数据，接着继续构建组【部门为Finance且员工薪资小于 3000】，构建完成后，可以在 Add Groups to Group 标签栏看到新建的两个组：

![](http://docs.mlsql.tech/upload_images/f26531e9-72f3-4908-b14c-1b1ba8ae8cfc.png)

在这里，我们可以进一步组合组，得到最终组，

![](http://docs.mlsql.tech/upload_images/559d7f2e-438d-4f1b-985f-0262a9a6cf4e.png)

现在可以切到 Apply Group里，选择最终组

![](http://docs.mlsql.tech/upload_images/69f5a138-1740-4615-b89b-1afda3713bc8.png)

点击Apply,即可看到结果：

![](http://docs.mlsql.tech/upload_images/254d6abf-7ebd-43b5-914b-112e45f3dc9c.png)









