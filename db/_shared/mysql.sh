#!/bin/sh
# Клієнт mysql усередині контейнера duikt-mysql. SQL читається зі stdin або з аргументів.
# Приклад: _shared/mysql.sh -t -vvv car_rental < queries.sql
exec docker exec -i -e MYSQL_PWD=root_dev duikt-mysql mysql -uroot --default-character-set=utf8mb4 "$@"
