# 脚本插件 save_excel

用户可以通过安装比较重的数据源插件来完成相同的功能。参考[网络安装插件](http://docs.mlsql.tech/mlsql-engine/plugin/online_install.html)。
在前文中，我们通过动态安装jar包的方式，使得我们能够操作excel数据源。

对于读取，我们可以通过参数`maxRowsInMemory`很好的控制内存。但是对于写入，该数据源插件则没有相关控制内存的使用参数，这导致保存较大的excel文件时，
内存使用过大，影响整个Engine的稳定性。

通过 脚本插件，`save_excel`，我们不仅可以避免影响系统稳定性，同时完全不需要安装插件，只需要使用`include` 语法引用即可。

参看如下使用代码：

```sql
select 1 as a, 2 as b as mockTable;

--如果是在demo站点，加上下面的配置，因为该插件需要python支持
-- !python env "PYTHON_ENV=source /usr/local/miniconda/bin/activate dev";


set inputTable = "mockTable";
set outputPath = "/tmp/jack.xlsx";
include store.`william/save_excel`;
```

这里，我们随意模拟了一张表 mockTable,然后通过set 语法配置两个参数：

1. inputTable
2. outputPath

接着 include 商店里的save_excel插件即可。

点击执行，就可以在/tmp目录里看到jack.xlsx数据啦。

如果用户么有办法访问公网，可以通过如下python代码在有网络的环境下获取脚本内容：

```python
import requests
import json
res = requests.post("http://store.mlsql.tech/run",
              params={
                  "action":"getPlugin",
                  "pluginName":'''william/save_excel''',
                  "version": "0.1.0","pluginType":"MLSQL_SCRIPT"
              })
json.loads(json.loads(res.content)[0]["extraParams"])["content"]
```

然后拷贝出对应的代码，替换`include store.`save_excel`;`这一行，可以取得一样的效果。