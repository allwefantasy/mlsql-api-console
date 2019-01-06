FROM debian:latest

#  $ docker build . -t continuumio/miniconda3:latest -t continuumio/miniconda3:4.5.11
#  $ docker run --rm -it continuumio/miniconda3:latest /bin/bash
#  $ docker push continuumio/miniconda3:latest
#  $ docker push continuumio/miniconda3:4.5.11

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV PATH /opt/conda/bin:$PATH

RUN apt-get update --fix-missing && \
    apt-get install -y openjdk-8-jre-headless wget bzip2 ca-certificates curl git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /home/deploy
RUN mkdir -p /home/deploy/mlsql-console


WORKDIR /home/deploy/mlsql-api-console

ENV MLSQL_CONSOLE_JAR=mlsql-api-console-1.0-SNAPSHOT.jar
ENV MLSQL_CONSOLE_CONFIG_FILE="application.docker.yml"
ADD ${MLSQL_CONSOLE_JAR} /home/deploy/mlsql-api-console/
ADD ${MLSQL_CONSOLE_CONFIG_FILE} /home/deploy/mlsql-api-console/
ADD start.sh /home/deploy/mlsql-api-console/

ENV MYSQL_HOST=mlsql-console-mysql
RUN sed -i "s/MYSQL_HOST/${MYSQL_HOST}/g" application.docker.yml

ENV MLSQL_CLUSTER_URL=http://127.0.0.1:9003
ENV MY_URL=http://127.0.0.1:9002
CMD ./start.sh
