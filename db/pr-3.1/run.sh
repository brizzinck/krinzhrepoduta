#!/bin/sh
# Запускає консольний додаток у docker у мережі контейнера duikt-mysql (порт БД на хост не публікується).
# Збірка: ../../tools/mvn.sh -q package. Паролі лише для локального контейнера.
cd "$(dirname "$0")"
TTY=""
[ -t 0 ] && TTY="-t"
exec docker run --rm -i $TTY -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 \
  --network container:duikt-mysql \
  -e DB_URL="${DB_URL:-jdbc:mysql://127.0.0.1:3306/car_rental}" \
  -e DB_USER="${DB_USER:-rental}" -e DB_PASSWORD="${DB_PASSWORD:-rental_dev}" \
  -v "$PWD":/w -w /w eclipse-temurin:21-jre java -jar target/rentalapp.jar
