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
  export PGPASSWORD=$6
  psql -h $5 -d $DATABASE_NAME -U $DATABASE_USER -p $7 -f /sql/$FILE_NAME.sql
else
  echo "executing for local"
  docker cp $1 am-lib-testing-service-db:/tmp/
  docker exec -i am-lib-testing-service-db psql -U $DATABASE_USER -d $DATABASE_NAME -f /tmp/sql/$FILE_NAME.sql
fi
echo "completed data load"
