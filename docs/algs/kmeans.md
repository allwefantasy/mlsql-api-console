# KMeans

KMeans 属于聚类算法。

首先我们新增一些数据。

```
set jsonStr='''
{"features":[5.1,3.5,1.4,0.2],"label":0.0},
{"features":[5.1,3.5,1.4,0.2],"label":1.0}
{"features":[5.1,3.5,1.4,0.2],"label":0.0}
{"features":[4.4,2.9,1.4,0.2],"label":0.0}
{"features":[5.1,3.5,1.4,0.2],"label":1.0}
{"features":[5.1,3.5,1.4,0.2],"label":0.0}
{"features":[5.1,3.5,1.4,0.2],"label":0.0}
{"features":[4.7,3.2,1.3,0.2],"label":1.0}
{"features":[5.1,3.5,1.4,0.2],"label":0.0}
{"features":[5.1,3.5,1.4,0.2],"label":0.0}
''';
load jsonStr.`jsonStr` as data;
select vec_dense(features) as features from data
as data1;
```

聚类算法属于无监督算法，所以没有Label的概念。接着，我们可以训练了：


```sql
train data1 as KMeans.`/tmp/alg/kmeans`
where k="2"
and seed="1";
```

训练时可以设置的参考包括：

1. k 聚类数，默认为2 (这样得到三个分类0,1,2)
2. initMode 初始化模式： 'random' and 'k-means||'
3. initSteps kmeans步数,必须大于0

## 批量预测

无

## API预测

训练完成后，可以注册模型为函数，进行预测：

```sql
register KMeans.`/tmp/alg/kmeans` as kcluster;
select kcluster(features) as catagory from data1 as output;
```


