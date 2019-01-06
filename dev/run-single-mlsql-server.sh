#!/usr/bin/env bash

SELF=$(cd $(dirname $0) && pwd)
. "$SELF/docker-command.sh"

#set -e
#set -o pipefail

docker run --name mlsql-server -d \
--network mlsql-network \
--host 
-p 9003:9003 \
techmlsql/mlsql:spark_2.3-1.1.7-SNAPSHOT