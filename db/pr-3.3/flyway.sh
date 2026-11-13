#!/bin/sh
# Flyway 11 CLI у docker у мережі контейнера БД. Міграції лежать у src/main/resources/db/migration.
# Існуюча база northwind_orm одразу стає базовою версією 1 (baselineOnMigrate), далі застосовуються V2 і новіші.
# Приклад: ./flyway.sh info   ./flyway.sh migrate
cd "$(dirname "$0")"
exec docker run --rm -u "$(id -u):$(id -g)" --network container:duikt-mysql \
  -v "$PWD/src/main/resources/db/migration":/flyway/sql:ro \
  flyway/flyway:11.20.3 \
  -url="jdbc:mysql://127.0.0.1:3306/northwind_orm" -user=rental -password=rental_dev \
  -locations=filesystem:/flyway/sql -baselineOnMigrate=true -baselineVersion=1 "$@"
