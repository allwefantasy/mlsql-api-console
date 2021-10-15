#!/usr/bin/env bash

SELF=$(cd $(dirname $0) && pwd)
ROOT=$(cd ${SELF}/.. && pwd)
cd ${ROOT}

## compile console project
mvn_version=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
mvn clean install -DskipTests -Pshade

## build package tar
export package_name=mlsql-console-${mvn_version}
[[ ! -d "dist" ]] && mkdir -p "dist" || rm -rf dist/*
mkdir -p dist/${package_name}

cd dist/${package_name}

### 1. make commit_sha file
echo `git rev-parse HEAD` | tee commit_SHA1

### 2. make version file
echo "${mvn_version}" > VERSION

### 3. copy console jar file
cp ${ROOT}/target/mlsql-api-console-${mvn_version}.jar .
mv mlsql-api-console-${mvn_version}.jar mlsql-console.jar

### 4. copy other resources
cp ${ROOT}/config/application.docker.yml .
cp ${ROOT}/config/logging.yml .
cp ${ROOT}/console-db.sql .
cp ${SELF}/docker/start.sh .
cp ${SELF}/start-default.sh .

### 5. make readme.md
cat > README.md << EOF
1. Use "db.sql" to create db and tables;
2. Modify application.docker.yml according to  mysql you had configured.
3. Run ./start-default.sh

Notice that MLSQL Console should knows where is the MLSQL Cluster and the the ip he owns,
You can change them in start-default.sh.
EOF

### 6. make tar package
cd ${ROOT}/dist
tar -zcvf ${package_name}.tar.gz ${package_name}

echo "====================================="
echo "Build Finished!"
echo "Location: ${ROOT}/dist/${package_name}.tar.gz"