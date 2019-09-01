@echo off
set "DATABASE_USER=%3"
set "DATABASE_NAME=%4"

if "%DATABASE_USER%" == "" (
    set "DATABASE_USER=amuser"
)
if "%DATABASE_NAME%" == "" (
    set "DATABASE_NAME=am"
)
if "%2" == "load" (
  set "FILE_NAME=load-data"
) ELSE (
    set "FILE_NAME=delete-data"
)
docker cp %1  am-api-db:/tmp/
docker exec -i am-api-db psql -U %DATABASE_USER% -d %DATABASE_NAME% -f /tmp/sql/%FILE_NAME%.sql
echo done
