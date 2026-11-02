#!/bin/sh
# Створює бази з нуля: car_rental (основна) і car_rental_check (копія для перевірки обмежень).
# Запуск: ./rebuild.sh  (контейнер duikt-mysql має працювати: ../_shared/mysql-up.sh)
set -eu
cd "$(dirname "$0")"
M=../_shared/mysql.sh
$M < schema.sql
$M < seed.sql
DROP='DROP DATABASE IF EXISTS car_rental_check;'
echo "$DROP" | $M
sed 's/car_rental/car_rental_check/g' schema.sql | $M
sed 's/car_rental/car_rental_check/g' seed.sql | $M
echo "бази car_rental і car_rental_check створено"
