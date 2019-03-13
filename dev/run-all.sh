#!/usr/bin/env bash

SELF=$(cd $(dirname $0) && pwd)
cd $SELF
cd ..

cd /tmp

function docker_exec {
  name=$1
  command=$2
  #test -t 1 && USE_TTY="t"
  docker exec -i${USE_TTY} ${name} bash -c "${command}"
}

function check_ready {
    recCode="1"
    counter=0
    name=$1
    command=$2
    while [[ $counter -lt 60 ]] ; do
      docker_exec "${1}" "${command}"
      recCode="$?"
      if [[ "$recCode" == "0" ]]; then
          echo "check success. try ${counter} times."
          break
      fi
      sleep 1
      echo "try ${counter} times."
      let "counter+=1"
    done
    echo $recCode
}

echo "----clean all mlsql related containers-----"

ids=$(docker ps --all |grep mlsql|awk '{print $1}')
for v in $ids
do
echo $v
docker stop $v
docker rm $v
done

VERSION=1.2.0-SNAPSHOT

echo "------create mlsql-network-----"
docker network rm  mlsql-network
docker network create mlsql-network

echo "------create mlsql-engine------"

docker run --name mlsql-server -d \
--network mlsql-network \
-p 9003:9003 \
techmlsql/mlsql:spark_2.4-${VERSION}

echo "start mysql"

docker run --name mlsql-console-mysql --network mlsql-network  -e MYSQL_ROOT_PASSWORD=mlsql -d mysql:5.7


EXEC_MLSQL_PREFIX="exec mysql -uroot -pmlsql --protocol=tcp "

check_ready mlsql-console-mysql "${EXEC_MLSQL_PREFIX} -e 'SHOW CHARACTER SET'"

if [[ "$?" != "0" ]];then
   echo "cannot start mysql in docker"
   exit 1
fi

echo "------create db mlsql cluster ------"

docker_exec mlsql-console-mysql "${EXEC_MLSQL_PREFIX} -e 'create database mlsql_cluster'"
docker_id=$(docker inspect -f   '{{.Id}}' mlsql-console-mysql)
wget download.mlsql.tech/scripts/cluster-db.sql .
docker cp cluster-db.sql ${docker_id}:/tmp
docker_exec mlsql-console-mysql "${EXEC_MLSQL_PREFIX} mlsql_cluster < /tmp/cluster-db.sql"


echo "------create db mlsql_console ------"
docker_exec mlsql-console-mysql "${EXEC_MLSQL_PREFIX} -e 'create database mlsql_console'"

#导入数据
docker_id=$(docker inspect -f   '{{.Id}}' mlsql-console-mysql)
wget download.mlsql.tech/scripts/console-db.sql .
docker cp console-db.sql ${docker_id}:/tmp
docker_exec mlsql-console-mysql "${EXEC_MLSQL_PREFIX} mlsql_console < /tmp/console-db.sql"


echo "------start mlsql cluster ------"

docker run --name mlsql-cluster \
-d --network mlsql-network \
-p 8080:8080 \
techmlsql/mlsql-cluster:${VERSION}


echo "------start mlsql console ------"
docker run --name mlsql-console \
-d --network mlsql-network \
-p 9002:9002 \
-e MLSQL_CLUSTER_URL=http://mlsql-cluster:8080 \
-e MY_URL=http://mlsql-console:9002 \
-e USER_HOME=/home/users \
techmlsql/mlsql-console:${VERSION}







