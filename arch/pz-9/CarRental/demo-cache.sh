#!/bin/bash
# Показує роботу кешу на запущеному API (./serve.sh start).
# PART=reads лише рахує SQL на читання: так само можна виміряти код без кешу.
set -u
BASE=${BASE:-http://127.0.0.1:5080}
PART=${PART:-all}
API=carrental-api
CACHE=carrental-memcached
day() { date -d "$1 days" +%F; }
sql_total() { docker logs "$API" 2>&1 | grep -c 'Executed DbCommand'; }
json() { python3 -c "import sys,json; print(json.load(sys.stdin)$1)"; }

# Читає оренду: друкує код, час, статус і кількість SQL саме цього запиту.
read_rental() {
  local before after out
  before=$(sql_total)
  out=$(curl -s -o /tmp/rental.$$ -w 'HTTP %{http_code}, %{time_total} с' "$BASE/api/rentals/$RENTAL")
  sleep 0.3
  after=$(sql_total)
  echo "$1: $out, статус $(json "['status']" < /tmp/rental.$$), SQL-запитів: $((after - before))"
}
trap 'rm -f /tmp/rental.$$' EXIT

CAR=$(curl -s "$BASE/api/cars" | python3 -c "import sys,json; print([c['id'] for c in json.load(sys.stdin) if c['plate']=='BB2222CC'][0])")
RENTER=$(curl -s -X POST "$BASE/api/renters" -H 'Content-Type: application/json' -d "{\"fullName\":\"Іваненко Іван Іванович\",\"email\":\"ivan@example.com\",\"birthDate\":\"1990-05-20\",\"licenseNumber\":\"ABC123456\",\"licenseValidUntil\":\"$(day +1500)\"}" | json "['id']")
open_rental() { curl -s -X POST "$BASE/api/rentals" -H 'Content-Type: application/json' -d "{\"carId\":\"$CAR\",\"renterId\":\"$RENTER\",\"start\":\"$(day "+$1")\",\"end\":\"$(day "+$2")\"}" | json "['id']"; }
RENTAL=$(open_rental 1 4)

echo "1. Два читання однієї оренди:"
read_rental "перше"
read_rental "друге"
[ "$PART" = reads ] && exit 0

stats() { docker exec "$CACHE" sh -c 'printf "stats\r\nquit\r\n" | nc 127.0.0.1 11211' | tr -d '\r' | grep -E 'STAT (get_hits|get_misses|curr_items) '; }
echo
echo "2. Лічильники сервера Memcached:"
stats

echo
echo "3. Інвалідація: завершення оренди, потім читання:"
curl -s -o /dev/null -w 'POST complete: HTTP %{http_code}\n' -X POST "$BASE/api/rentals/$RENTAL/complete" -H 'Content-Type: application/json' -d '{"endMileage":31900}'
read_rental "після завершення"
read_rental "ще раз"

echo
echo "4. Memcached зупинено, застосунок читає з БД:"
docker rm -f "$CACHE" >/dev/null
read_rental "кеш вимкнено, перше"
read_rental "кеш вимкнено, друге"

echo
echo "5. Memcached знову запущено:"
docker run -d --name "$CACHE" --network arch --network-alias memcached memcached:1.6-alpine -m 64 >/dev/null
sleep 15
read_rental "після повернення, перше"
read_rental "після повернення, друге"
stats

echo
echo "6. Зміна в обхід застосунку: кеш віддає старе до кінця TTL:"
RENTAL=$(open_rental 10 12)
read_rental "прочитано в кеш"
python3 - "$RENTAL" <<'PY'
import sqlite3, sys
c = sqlite3.connect('.data/carrental.db')
n = c.execute("update Rentals set Status='Cancelled' where upper(Id)=upper(?)", (sys.argv[1],)).rowcount
c.commit()
print(f'UPDATE напряму в БД: змінено рядків {n}')
PY
read_rental "одразу після UPDATE"
sleep 61
read_rental "через 61 с"
