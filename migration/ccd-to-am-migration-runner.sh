#!/bin/sh

echo
set -e
set -u

DB="am"
USER="amuser"
INIT_SQL="./ccd-to-am-migration-init.sql"
MAIN_SQL="./ccd-to-am-migration-main.sql"

psql \
    -X \
    -q \
    -U $USER \
    -f $INIT_SQL \
    --set AUTOCOMMIT=off \
    --set ON_ERROR_STOP=off \
    $DB

psql \
    -X \
    -q \
    -U $USER \
    -f $MAIN_SQL \
    --set AUTOCOMMIT=off \
    --set ON_ERROR_STOP=off \
    $DB

echo "migration successful"
exit 0
