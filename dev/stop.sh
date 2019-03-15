ids=$(docker ps --all |grep mlsql|awk '{print $1}')
for v in $ids
do
echo $v
docker stop $v
docker rm $v
done
