# dataMode 详解

在使用!ray命令时，dataMode是必须设置的。dataMode可选值为 data/model. 那么他们到底
是什么含义呢？

## data

简单场景是，如果你使用了foreach/map_iter 等高阶函数，并且设置了Ray地址，则使用data模式。
如果从更深入的角度来看，就是你的数据会经过ray分布式处理并且不通过ray client端回流到MLSQL,则需要设置为data模式。

## model

如果你只是用ray client,则使用model即可。
