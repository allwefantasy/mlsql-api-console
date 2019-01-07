#!/usr/bin/env bash
#set -e
#set -o pipefail

docker run --name mlsql-server -d \
--network mlsql-network \
--host 
-p 9003:9003 \
techmlsql/mlsql:spark_2.3-1.1.7-SNAPSHOT