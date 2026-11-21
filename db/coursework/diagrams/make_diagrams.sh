#!/bin/sh
# Build the three models: infological (Chen, neato), datalogical and physical (crow's foot, dot).
# The datalogical and physical models are read from information_schema of a running MySQL container.
#
# Usage:
#   make_diagrams.sh --throwaway          start a temporary mysql:8.4 container (cw-diag-mysql) from the V1 DDL,
#                                         draw, remove it; DDL=path/to/V1__schema.sql overrides the default path
#   make_diagrams.sh CONTAINER            use a running container, e.g. the compose db:
#                                         DB_PASSWORD=... make_diagrams.sh micromobility-db-1
# Environment for CONTAINER mode: DB_USER (default root), DB_PASSWORD, DB_NAME (default micromobility).
set -eu
cd "$(dirname "$0")"

DDL=${DDL:-../app/src/main/resources/db/migration/V1__schema.sql}
TARGET=${1:---throwaway}

if [ "$TARGET" = "--throwaway" ]; then
  [ -f "$DDL" ] || { echo "error: DDL file not found: $DDL" >&2; exit 1; }
  DDL_ABS=$(cd "$(dirname "$DDL")" && pwd)/$(basename "$DDL")
  export DB_CONTAINER=cw-diag-mysql DB_USER=root DB_PASSWORD=diag DB_NAME=micromobility
  docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
  trap 'docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true' EXIT
  docker run -d --name "$DB_CONTAINER" -e MYSQL_ROOT_PASSWORD="$DB_PASSWORD" -e MYSQL_DATABASE="$DB_NAME" \
    -v "$DDL_ABS:/docker-entrypoint-initdb.d/V1__schema.sql:ro" mysql:8.4 >/dev/null
  # The image first runs a temporary server for the init scripts; wait for the real one after that.
  i=0
  until docker logs "$DB_CONTAINER" 2>&1 | grep -q "init process done" \
     && docker exec -e MYSQL_PWD="$DB_PASSWORD" "$DB_CONTAINER" mysqladmin -uroot ping >/dev/null 2>&1; do
    i=$((i + 1)); [ "$i" -le 90 ] || { echo "error: database did not start" >&2; exit 1; }
    sleep 2
  done
else
  export DB_CONTAINER=$TARGET DB_USER=${DB_USER:-root} DB_NAME=${DB_NAME:-micromobility}
  export DB_PASSWORD=${DB_PASSWORD:?set DB_PASSWORD for the target container}
fi

mkdir -p img
python3 gen_infological.py > infological.dot
python3 gen_datalogical.py > datalogical.dot
python3 gen_physical.py > physical.dot

neato -Tpng -Gdpi=170 infological.dot -o img/infological.png
neato -Tsvg infological.dot -o img/infological.svg
for m in datalogical physical; do
  dot -Tpng -Gdpi=150 "$m.dot" -o "img/$m.png"
  dot -Tsvg "$m.dot" -o "img/$m.svg"
done
echo "diagrams built in $(pwd)/img"
