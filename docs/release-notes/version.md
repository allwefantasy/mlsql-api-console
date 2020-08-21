# MLSQL Stack版本管理策略

从2.0.0版本开始，我们对MLSQL Console/MLSQL Engine的版本策略进行了修改。


## 版本规划

1. Bug修复都会在小版本里增加。比如2.0.1,2.0.2
2. 新特性都会在中间版本增加， 如2.1.0,2.2.0

## Console/Engine版本兼容规则

小版本MLSQL Console高版本会兼容MLSQL Engine 低版本。比如 Console 2.0.2 会兼容 Engine 2.0.0/2.0.1,
这意味着小版本Console/Engine会单独发布。

新特性版本，则会同步发布。比如Console 2.1.0 发布，那么Engine也一定会发布2.1.0。
