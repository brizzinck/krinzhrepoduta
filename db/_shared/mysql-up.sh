#!/bin/sh
# Піднімає (або запускає, якщо вже створений) локальний контейнер MySQL 8.4 для робіт із БД.
# Дані лежать у docker-томі duikt-mysql-data, порт на хост не публікується:
# клієнт запускається всередині контейнера (див. mysql.sh).
# Паролі — лише для локального контейнера.
set -eu
NAME=duikt-mysql
if docker inspect "$NAME" >/dev/null 2>&1; then
  docker start "$NAME" >/dev/null
else
  docker run -d --name "$NAME" \
    -e MYSQL_ROOT_PASSWORD=root_dev \
    -v duikt-mysql-data:/var/lib/mysql \
    mysql:8.4 \
    --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci >/dev/null
fi
# чекаємо готовності сервера
for i in $(seq 1 60); do
  if docker exec -e MYSQL_PWD=root_dev "$NAME" mysqladmin -uroot ping --silent >/dev/null 2>&1; then
    echo "MySQL готовий"; break
  fi
  sleep 2
done
# користувач застосунків (для наступних робіт): права лише на бази проєкту
docker exec -i -e MYSQL_PWD=root_dev "$NAME" mysql -uroot <<'SQL'
CREATE USER IF NOT EXISTS 'rental'@'%' IDENTIFIED BY 'rental_dev';
GRANT ALL PRIVILEGES ON `car_rental`.* TO 'rental'@'%';
GRANT ALL PRIVILEGES ON `car_rental_%`.* TO 'rental'@'%';
SQL
