#!/bin/bash
# Показує ціну оренди на вихідні (пт-пн, три доби, економ-клас) за поточного налаштування API.
# Запуск без надбавки:  ./serve.sh reset; ./serve.sh start; ./demo-pricing.sh
# Запуск з надбавкою:   ./serve.sh reset; EXTRA_ENV='-e Pricing__WeekendSurcharge=true' ./serve.sh start; ./demo-pricing.sh
set -u
BASE=${BASE:-http://127.0.0.1:5080}
FRIDAY=$(date -d "next friday" +%F); MONDAY=$(date -d "$FRIDAY +3 days" +%F)
CAR=$(curl -s "$BASE/api/cars" | python3 -c "import sys,json; print([c['id'] for c in json.load(sys.stdin) if c['plate']=='AA0001BB'][0])")
RENTER=$(curl -s -X POST "$BASE/api/renters" -H 'Content-Type: application/json' \
  -d "{\"fullName\":\"Іваненко Іван Іванович\",\"email\":\"ivan@example.com\",\"birthDate\":\"1990-05-20\",\"licenseNumber\":\"ABC123456\",\"licenseValidUntil\":\"$(date -d '+1500 days' +%F)\"}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "> POST /api/rentals  Skoda Fabia (800 грн/доба), $FRIDAY .. $MONDAY (п'ятниця, субота, неділя)"
curl -s -X POST "$BASE/api/rentals" -H 'Content-Type: application/json' \
  -d "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$FRIDAY\",\"end\":\"$MONDAY\"}" \
  | python3 -c "import sys,json; r=json.load(sys.stdin); print(json.dumps({k:r[k] for k in ('period','total','status')},ensure_ascii=False,indent=4))"
