# MLSQL K8s部署，镜像环境如何制作

通常我会制作两个镜像：

基础镜像，该镜像需要满足如下几点：

1. JDK
2. Conda(Python相关环境)
3. Spark发型包

我们以Spark 3.0为例，假设我们镜像名称叫： spark:v3.0.0-hadoop3.2

注： 该镜像文件需要在Spark发行版根目录执行。

```
ARG java_image_tag=14.0-jdk-slim

FROM openjdk:${java_image_tag}

ARG spark_uid=185

RUN set -ex && \
    apt-get update && \
    ln -s /lib /lib64 && \
    apt install -y bash tini libc6 libpam-modules krb5-user libnss3 && \
    mkdir -p /opt/spark && \
    mkdir -p /opt/spark/examples && \
    mkdir -p /opt/spark/work-dir && \
    touch /opt/spark/RELEASE && \
    rm /bin/sh && \
    ln -sv /bin/bash /bin/sh && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    rm -rf /var/cache/apt/*

RUN apt-get update \
    && apt-get install -y \
        git \
        wget \
        cmake \
        build-essential \
        curl \
        unzip \
        libgtk2.0-dev \
        zlib1g-dev \
        libgl1-mesa-dev \
    && apt-get clean \
    && echo 'export PATH=/opt/conda/bin:$PATH' > /etc/profile.d/conda.sh \
    && wget \
        --quiet "https://repo.anaconda.com/miniconda/Miniconda3-4.7.12.1-Linux-x86_64.sh" \
        -O /tmp/anaconda.sh \
    && /bin/bash /tmp/anaconda.sh -b -p /opt/conda \
    && rm /tmp/anaconda.sh \
    && /opt/conda/bin/conda install -y \
        libgcc python=3.6.9 \
    && /opt/conda/bin/conda clean -y --all \
    && /opt/conda/bin/pip install \
        flatbuffers \
        cython==0.29.0 \
        numpy==1.15.4

RUN /opt/conda/bin/conda create --name dev python=3.6.9 -y \
    && source /opt/conda/bin/activate  dev \
    && pip install pyarrow==0.10.0 \
    && pip install ray==0.8.0 \
    && pip install aiohttp psutil setproctitle grpcio pandas xlsxwriter watchdog requests click uuid sfcli  pyjava

COPY jars /opt/spark/jars
COPY bin /opt/spark/bin
COPY sbin /opt/spark/sbin
COPY kubernetes/dockerfiles/spark/entrypoint.sh /opt/
COPY examples /opt/spark/examples
COPY kubernetes/tests /opt/spark/tests
COPY data /opt/spark/data

ENV SPARK_HOME /opt/spark

WORKDIR /opt/spark/work-dir
RUN chmod g+w /opt/spark/work-dir

ENTRYPOINT [ "/opt/entrypoint.sh" ]

USER ${spark_uid}
```


接着，我们会基于该镜像，打包MLSQL相关的依赖：

```
FROM spark:v3.0.0-hadoop3.2
COPY streamingpro-mlsql-spark_3.0_2.12-2.0.1-SNAPSHOT.jar /opt/spark/work-dir/
WORKDIR /opt/spark/work-dir
ENTRYPOINT [ "/opt/entrypoint.sh" ]
```

通常基础镜像不太用变化。MLSQL倒是可能会升级较为频繁。

