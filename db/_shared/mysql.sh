#!/bin/sh
# Клієнт mysql усередині контейнера duikt-mysql. SQL читається зі stdin або з аргументів.
# Помилки зливаються у stdout усередині контейнера, щоб у виводі стояти на своїх місцях.
# Приклад: _shared/mysql.sh -t -vvv car_rental < queries.sql
# Інший користувач: MYSQL_PWD=пароль _shared/mysql.sh -uімʼя -t northwind < запит.sql
exec docker exec -i -e MYSQL_PWD="${MYSQL_PWD:-root_dev}" duikt-mysql \
  sh -c 'exec mysql "$@" 2>&1' sh -uroot --default-character-set=utf8mb4 "$@"
