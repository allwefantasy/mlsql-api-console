# Readiness探针

> MLSQL Engine 2.1.0-SNAPSHOT及以上可用


MLSQL 支持K8s的Readiness探针。对应接口为

```
http://..../health/readiness
```
如果已经初始化完成，处于可用状态，返回200,结果如下：

```json
// 20201029141214
// http://127.0.0.1:9003/health/readiness

{
  "status": "IN_SERVICE",
  "components": {
    "readinessProbe": {
      "status": "IN_SERVICE"
    }
  }
}
```

如果系统还未初始化完成，返回503,结果如下：

```json
// 20201029141214
// http://127.0.0.1:9003/health/readiness

{
  "status": "OUT_OF_SERVICE",
  "components": {
    "readinessProbe": {
      "status": "OUT_OF_SERVICE"
    }
  }
}
```

