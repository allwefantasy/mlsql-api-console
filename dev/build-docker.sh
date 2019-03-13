#!/usr/bin/env bash

SELF=$(cd $(dirname $0) && pwd)
cd $SELF
cd ..

if [[ -z "${MLSQL_CONSOLE_VERSION}" ]];then
MLSQL_CONSOLE_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
fi

mvn clean package -Pshade
cp target/mlsql-api-console-${MLSQL_CONSOLE_VERSION}.jar ./dev/docker
cd $SELF

docker build --build-arg MLSQL_CONSOLE_JAR=mlsql-api-console-${MLSQL_CONSOLE_VERSION}.jar -t mlsql-console:${MLSQL_CONSOLE_VERSION} ./docker
