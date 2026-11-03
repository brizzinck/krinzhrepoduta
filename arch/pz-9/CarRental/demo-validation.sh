#!/bin/bash
# Некоректні запити до запущеного API (./serve.sh start): показує код і тіло відповіді.
set -u
BASE=${BASE:-http://127.0.0.1:5080}
day() { date -d "$1 days" +%F; }
post() {
  echo "> POST $1 $2"
  curl -s -o /tmp/body.$$ -w 'HTTP %{http_code}\n' -X POST "$BASE$1" -H 'Content-Type: application/json' -d "$2"
  python3 -m json.tool --no-ensure-ascii /tmp/body.$$ 2>/dev/null || cat /tmp/body.$$
  echo
}
trap 'rm -f /tmp/body.$$' EXIT
count_cars() { curl -s "$BASE/api/cars" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))"; }

echo "Автомобілів до запитів: $(count_cars)"
echo
echo "1. Автомобіль: кілька помилок формату одразу:"
post /api/cars '{"plate":"123","model":"","class":"Economy","dailyRate":-5,"mileage":-1}'
echo "2. Автомобіль: невідомий клас (семантика):"
post /api/cars '{"plate":"ZZ9999ZZ","model":"Bus","class":"Bus","dailyRate":500,"mileage":0}'
echo "3. Автомобіль: номер уже існує (перевірка через БД):"
post /api/cars '{"plate":"AA0001BB","model":"Copy","class":"Economy","dailyRate":500,"mileage":0}'
echo "4. Орендар: кілька помилок формату:"
post /api/renters '{"fullName":"","email":"not-mail","birthDate":"1990-05-20","licenseNumber":"1","licenseValidUntil":"2035-01-01"}'
echo "5. Оренда: порожні ідентифікатори й кінець раніше початку:"
post /api/rentals "{\"carId\":\"00000000-0000-0000-0000-000000000000\",\"renterId\":\"00000000-0000-0000-0000-000000000000\",\"start\":\"$(day +5)\",\"end\":\"$(day +1)\"}"
CAR=$(curl -s "$BASE/api/cars" | python3 -c "import sys,json; print([c['id'] for c in json.load(sys.stdin) if c['plate']=='AA0001BB'][0])")
YOUNG=$(curl -s -X POST "$BASE/api/renters" -H 'Content-Type: application/json' -d "{\"fullName\":\"Петренко Петро\",\"email\":\"petro@example.com\",\"birthDate\":\"$(date -d '-20 years' +%F)\",\"licenseNumber\":\"XYZ987654\",\"licenseValidUntil\":\"$(day +2)\"}" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "6. Оренда: орендар 20 років, посвідчення закінчується раніше, початок у минулому (перевірки через БД):"
post /api/rentals "{\"carId\":\"$CAR\",\"renterId\":\"$YOUNG\",\"start\":\"$(day -1)\",\"end\":\"$(day +4)\"}"
echo "Автомобілів після запитів: $(count_cars)"
echo "Оренд цього орендаря: $(curl -s "$BASE/api/rentals?renterId=$YOUNG" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")"
