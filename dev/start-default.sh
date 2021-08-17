#!/usr/bin/env bash

export MLSQL_CONSOLE_JAR="mlsql-console.jar"
export MLSQL_CLUSTER_URL=http://127.0.0.1:8080
export MY_URL=http://127.0.0.1:9002
export USER_HOME=/home/users
export ENABLE_AUTH_CENTER=false
export MLSQL_CONSOLE_CONFIG_FILE=application.docker.yml

SELF=$(cd $(dirname $0) && pwd)
cd $SELF

./start.sh
