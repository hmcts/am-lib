#!/bin/sh
DATABASE_USER=$3;
DATABASE_NAME=$4;

if [ -z "$DATABASE_USER" ] ; then
    DATABASE_USER="amuser";
fi

if [ -z "$DATABASE_NAME" ]; then
  DATABASE_NAME="am"
fi

if [ "$2" = "load" ]; then
    FILE_NAME="initial-data-load"
else
    FILE_NAME="delete-data"
fi

docker cp $1 am-lib-testing-service-db:/tmp/
docker exec -i am-lib-testing-service-db psql -U $DATABASE_USER -d $DATABASE_NAME -f /tmp/testdata/$FILE_NAME.sql
