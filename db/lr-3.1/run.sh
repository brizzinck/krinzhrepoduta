#!/bin/sh
# Запускає ConnectDemo.java у docker (JDK, конектор із копії jar ПР 3.1 у pr-3.1) у мережі контейнера duikt-mysql.
# Передумови: контейнер duikt-mysql працює, база car_rental створена (make db).
cd "$(dirname "$0")"
exec docker run --rm -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 --network container:duikt-mysql \
  -e DB_URL=jdbc:mysql://127.0.0.1:3306/car_rental -e DB_USER=rental -e DB_PASSWORD=rental_dev \
  -v "$PWD":/w -w /w --entrypoint java maven:3.9-eclipse-temurin-21 \
  -cp pr-3.1/target/rentalapp.jar ConnectDemo.java
