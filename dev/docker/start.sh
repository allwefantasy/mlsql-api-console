#!/usr/bin/env bash
echo "========"
echo "connect mysql: ${MYSQL_HOST}"
sed -i "s/MYSQL_HOST/${MYSQL_HOST}/g" ${MLSQL_CONSOLE_CONFIG_FILE}

echo "config file:"
cat  ${MLSQL_CONSOLE_CONFIG_FILE}

java -cp .:${MLSQL_CONSOLE_JAR} tech.mlsql.MLSQLConsole \
-mlsql_engine_url ${MLSQL_ENGINE_URL} \
-my_url ${MY_URL} \
-user_home ${USER_HOME} \
-enable_auth_center ${ENABLE_AUTH_CENTER:-false} \
-config ${MLSQL_CONSOLE_CONFIG_FILE}