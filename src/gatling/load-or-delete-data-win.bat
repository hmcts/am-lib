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
  set "FILE_NAME=initial-data-load"
) ELSE (
    set "FILE_NAME=delete-data"
)
docker cp %1  am-lib-testing-service-db:/tmp/
docker exec -i am-lib-testing-service-db psql -U %DATABASE_USER% -d %DATABASE_NAME% -f /tmp/testdata/%FILE_NAME%.sql
echo done
