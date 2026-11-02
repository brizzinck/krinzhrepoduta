#!/bin/bash
# Демонстрація сценаріїв прокату справжніми HTTP-запитами до запущеного API (./serve.sh start).
# Кожен запуск створює нових орендарів і нові оренди; автомобілі беруться з початкових даних.
set -u
TMPBODY=$(mktemp)
trap 'rm -f "$TMPBODY"' EXIT
BASE=${BASE:-http://127.0.0.1:5080}
day() { date -d "+$1 days" +%F; }
years_ago() { date -d "-$1 years" +%F; }

# call МЕТОД ШЛЯХ [JSON]: друкує запит, код відповіді та тіло; тіло зберігається в $BODY.
call() {
  local method=$1 path=$2 data=${3:-}
  if [ -n "$data" ]; then echo "> $method $path $data"; else echo "> $method $path"; fi
  if [ -n "$data" ]; then
    CODE=$(curl -s -o $TMPBODY -w '%{http_code}' -X "$method" "$BASE$path" -H 'Content-Type: application/json' -d "$data")
  else
    CODE=$(curl -s -o $TMPBODY -w '%{http_code}' -X "$method" "$BASE$path")
  fi
  BODY=$(cat $TMPBODY)
  echo "HTTP $CODE"
  echo "$BODY" | python3 -m json.tool --no-ensure-ascii 2>/dev/null || echo "$BODY"
  echo
}
jget() { python3 -c "import sys,json; print(json.load(sys.stdin)$1)" <<<"$BODY"; }

echo "1. Автомобілі автопарку, початкові дані:"
call GET /api/cars
CAR=$(python3 -c "import json; print([c['id'] for c in json.load(open('$TMPBODY')) if c['plate']=='AA0001BB'][0])")

echo "2. Реєстрація орендарів, дорослого і 20-річного:"
call POST /api/renters "{\"fullName\":\"Іваненко Іван Іванович\",\"email\":\"ivan@example.com\",\"birthDate\":\"1990-05-20\",\"licenseNumber\":\"ABC123456\",\"licenseValidUntil\":\"$(day 1500)\"}"
RENTER=$(jget "['id']")
call POST /api/renters "{\"fullName\":\"Петренко Петро Петрович\",\"email\":\"petro@example.com\",\"birthDate\":\"$(years_ago 20)\",\"licenseNumber\":\"XYZ987654\",\"licenseValidUntil\":\"$(day 1500)\"}"
YOUNG=$(jget "['id']")

echo "3. Успішна оренда на 3 дні, очікуємо 201 і суму 2400 (3 x 800):"
call POST /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$(day 1)\",\"end\":\"$(day 4)\"}"
RENTAL=$(jget "['id']")

echo "4. Повторна оренда того самого авто на перетинні дати, очікуємо 409:"
call POST /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$(day 2)\",\"end\":\"$(day 5)\"}"

echo "5. Орендар молодший за 21 рік, очікуємо 400:"
call POST /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$YOUNG\",\"start\":\"$(day 20)\",\"end\":\"$(day 22)\"}"

echo "6. Оренда на 40 днів, очікуємо 400:"
call POST /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$(day 30)\",\"end\":\"$(day 70)\"}"

echo "7. Читання оренди та оренд орендаря:"
call GET "/api/rentals/$RENTAL"
call GET "/api/rentals?renterId=$RENTER"

echo "8. Завершення оренди з пробігом 42450, очікуємо 200 і новий пробіг авто:"
call POST "/api/rentals/$RENTAL/complete" '{"endMileage":42450}'
call GET "/api/cars/$CAR"

echo "9. Скасування майбутньої оренди та повторне скасування, очікуємо 200, потім 400:"
call POST /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$(day 10)\",\"end\":\"$(day 12)\"}"
SECOND=$(jget "['id']")
call POST "/api/rentals/$SECOND/cancel"
call POST "/api/rentals/$SECOND/cancel"

echo "10. Неіснуючий ресурс, очікуємо 404:"
call GET "/api/rentals/00000000-0000-0000-0000-000000000001"

