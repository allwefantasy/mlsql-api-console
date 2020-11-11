# Liveness探针

> MLSQL Engine 2.1.0-SNAPSHOT及以上可用

MLSQL 支持K8s的liveness探针。对应接口为

```
http://.../health/liveness
```
如果处于可用状态，返回200,结果如下：

```json
// 20201029141025
// http://127.0.0.1:9003/health/liveness

{
  "status": "UP",
  "components": {
    "livenessProbe": {
      "status": "UP"
    }
  }
}
```

如果系统不可用，返回500,结果如下：

```json
// 20201029141025
// http://127.0.0.1:9003/health/liveness

{
  "status": "DOWN",
  "components": {
    "livenessProbe": {
      "status": "DOWN"
    }
  }
}
```

