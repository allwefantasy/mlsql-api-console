#!/usr/bin/env bash

SELF=$(cd $(dirname $0) && pwd)
. "$SELF/docker-command.sh"

docker run --name mlsql-console \
-d --network mlsql-network \
-p 9002:9002 \
-e MYSQL_HOST=127.0.0.1 \
-e MLSQL_CLUSTER_URL=http://mlsql-server:9003 \
-e MY_URL=http://mlsql-console:9002 \
mlsql-console:1.0