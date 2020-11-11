# 听说mlsql-cluster暂时不更新了，mlsql-cluster是个啥？

MLSQL Cluster其实蛮有误导性的。MLSQL Cluster其实就是一个代理，可以管理多个MLSQL Engine实例。
典型的功能如实现负载均衡。当然了，MLSQL Cluster还有很多负载均衡策略可选，比如尽量将请求转发给最清闲的Engine实例，
亦或是将请求发送给绝对资源剩余最多的实例，亦或是发送给所有引擎。

同时，MLSQL Cluster还能实现一些读写分离的工作。尽管他功能强大，但是考虑到部署的复杂度，我们还是暂时停止了对它的更新。
Console目前已经能管理多个Engine，并且在使用时选择需要的引擎，大部分场景是已经满足的。