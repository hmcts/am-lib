#!/bin/sh
echo "start data load"
DATABASE_USER=$3;
DATABASE_NAME=$4;

if [ -z "$DATABASE_USER" ] ; then
    DATABASE_USER="amuser";
fi

if [ -z "$DATABASE_NAME" ]; then
  DATABASE_NAME="am"
fi

if [ "$2" = "load" ]; then
    FILE_NAME="load-data"
else
    FILE_NAME="delete-data"
fi
if [ "$8" = "aat" ]; then
  echo "executing for aat and file name $FILE_NAME.sql"
  psql "dbname=$DATABASE_NAME sslmode=require" -h $5 -U $DATABASE_USER -p $7 -f /nightlyperformancedata/sql/$FILE_NAME.sql
else
  echo "executing for local"
  docker cp $1 am-accessmgmt-api-db:/tmp/
  docker exec -i am-accessmgmt-api-db psql -U $DATABASE_USER -d $DATABASE_NAME -f /tmp/sql/$FILE_NAME.sql
fi
echo "completed data load"
