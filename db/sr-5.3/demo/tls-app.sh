#!/bin/sh
# Рис. 1: застосунок ПР 3.1 підключається по TLS (sslMode=REQUIRED), поки він тримає з'єднання 8 с,
# сервер показує шифр цього з'єднання (SQL у sql/01-app-session-tls.sql). Усе в одному запуску й у тому порядку, як виводиться.
# Передумови: працює контейнер duikt-mysql, зібрано jar ПР 3.1 (cd pr-3.1 && ./mvn.sh -q package).
cd "$(dirname "$0")/.."
URL="jdbc:mysql://127.0.0.1:3306/car_rental?sslMode=REQUIRED"
printf '$ %s\n' "(sleep 8; echo 0) | DB_URL='$URL' pr-3.1/run.sh | sed -un 1p &"
(sleep 8; echo 0) | DB_URL="$URL" pr-3.1/run.sh | sed -un 1p &
sleep 4
printf '$ %s\n' "./mysql.sh -t -vvv < sql/01-app-session-tls.sql"
./mysql.sh -t -vvv < sql/01-app-session-tls.sql
wait
