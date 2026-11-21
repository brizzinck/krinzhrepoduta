#!/usr/bin/env bash
# Done-check of the micromobility coursework application.
#   ./check.sh          fresh volume: docker compose down -v, up --build, then all parts
#   CHECK_KEEP=1 ./check.sh   use the running stack as it is (development)
# Parts: 0 stack and migrations, 1 constraint script on a scratch database, 2 CRUD and business rules through the
# HTTP forms, 3 report pages against the SQL of sql/queries.sql. Output also goes to results/*.txt.
# Exit code 0 only if every check passed.
set -u
cd "$(dirname "$0")"
BASE="http://127.0.0.1:${APP_PORT:-18480}"
TMP=$(mktemp -d)
JAR="$TMP/cookies"
trap 'rm -rf "$TMP"' EXIT
mkdir -p results
PASS=0
FAIL=0
OUT=/dev/null

say() { echo "$*"; echo "$*" >> "$OUT"; }
ok() { PASS=$((PASS + 1)); say "ok   $1"; }
bad() { FAIL=$((FAIL + 1)); say "FAIL $1"; }
part() { OUT="results/$1.txt"; : > "$OUT"; P_PASS=$PASS; P_FAIL=$FAIL; say "== $2"; }
part_end() { say "-- $1: $((PASS - P_PASS)) ok, $((FAIL - P_FAIL)) failed"; }

# MySQL client inside the db container, as the application user (extra arguments go to mysql)
db() { docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" --default-character-set=utf8mb4 "$MYSQL_DATABASE" "$@"' sh "$@"; }
dbroot() { docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 "$@"' sh "$@"; }
q() { db -N -B -e "$1" 2>/dev/null | head -1; }

expect_eq() { if [ "$2" = "$3" ]; then ok "$1 (= $3)"; else bad "$1 (expected $2, got $3)"; fi; }
expect_page() { if printf '%s' "$PAGE" | grep -qF -- "$2"; then ok "$1: page says «$2»"; else bad "$1: page has no «$2» (status $STATUS, location ${LOC##*/}, flash: $(printf '%s' "$PAGE" | grep -o 'id="flash-[a-z]*">[^<]*' | head -1))"; fi; }

# POST a form, follow the redirect with the cookie jar (flash messages live in the session).
# Sets STATUS (of the POST), PAGE (the page that follows) and LOC.
submit() {
  local path=$1; shift
  local hdr
  hdr=$(curl -s -D - -o "$TMP/page" -c "$JAR" -b "$JAR" -X POST "$@" "$BASE$path")
  STATUS=$(printf '%s' "$hdr" | head -1 | awk '{print $2}')
  LOC=$(printf '%s' "$hdr" | tr -d '\r' | awk 'tolower($1)=="location:" {print $2}')
  if [ -n "$LOC" ]; then
    case "$LOC" in /*) LOC="$BASE$LOC";; esac
    curl -s -c "$JAR" -b "$JAR" -o "$TMP/page" "$LOC"
  fi
  PAGE=$(cat "$TMP/page")
}
get() { PAGE=$(curl -s -c "$JAR" -b "$JAR" "$BASE$1"); }

# ---------------------------------------------------------------- part 0
if [ "${CHECK_KEEP:-0}" != 1 ]; then
  docker compose down -v >/dev/null 2>&1
  docker compose up -d --build >/dev/null 2>&1 || { echo "FAIL: docker compose up --build failed"; exit 1; }
fi
for _ in $(seq 1 90); do
  [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/")" = 200 ] && break
  sleep 2
done

part part0-stack "part 0: stack and migrations"
expect_eq "home page status" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/")"
expect_eq "Flyway versions 1 and 2 succeeded" "1:1,2:1" "$(q "SELECT GROUP_CONCAT(CONCAT(version, ':', success) ORDER BY version) FROM flyway_schema_history")"
expect_eq "seed rides after the migration (checked first, before the tests change data)" 57 "$(q 'SELECT COUNT(*) FROM rides')" 
if docker compose logs app 2>&1 | grep -q SchemaManagementException; then bad "ddl-auto=validate: SchemaManagementException in the app log"; else ok "ddl-auto=validate passed (no SchemaManagementException in the app log)"; fi
expect_eq "tables, columns" "7:43" "$(q "SELECT CONCAT(COUNT(DISTINCT table_name), ':', COUNT(*)) FROM information_schema.columns WHERE table_schema = 'micromobility' AND table_name IN ('operators','tariffs','stations','vehicles','customers','rides','payments')")"
expect_eq "time zone tables loaded: Kyiv month of 2026-10-31 22:30 UTC" "2026-11-01 00:30:00" "$(q "SELECT CONVERT_TZ('2026-10-31 22:30:00', '+00:00', 'Europe/Kyiv')")"
part_end "part 0"

# ---------------------------------------------------------------- part 1: constraint script on a scratch database
part part1-constraints "part 1: sql/checks.sql on a scratch database (the application data is not touched)"
dbroot -e "DROP DATABASE IF EXISTS micromobility_check; CREATE DATABASE micromobility_check;" 2>/dev/null
dbroot micromobility_check < app/src/main/resources/db/migration/V1__schema.sql 2>/dev/null
dbroot micromobility_check < app/src/main/resources/db/migration/V2__seed.sql 2>/dev/null
dbroot --force micromobility_check < sql/checks.sql > results/checks.txt 2>&1
errors=$(grep -c '^ERROR' results/checks.txt)
total=$(grep -c '^-- N[0-9]* expect error' sql/checks.sql)
expect_eq "negative statements failed" "$total" "$errors"
grep -o '^-- N[0-9]* expect error [a-z_0-9]*' sql/checks.sql | awk '{print $2, $5}' > "$TMP/expected.txt"
grep '^ERROR' results/checks.txt | sed -E \
  -e "s/.*Check constraint '([a-z_0-9]+)'.*/\1/" \
  -e "s/.*for key '[a-z_]+\.([a-z_0-9]+)'.*/\1/" \
  -e 's/.*CONSTRAINT `([a-z_0-9]+)`.*/\1/' \
  -e 's/^ERROR ([0-9]+) .*/\1/' > "$TMP/actual.txt"
mismatch=$(paste -d' ' "$TMP/expected.txt" "$TMP/actual.txt" | awk '$2 != $3 {n++} END {print n + 0}')
expect_eq "negative statements that failed on another constraint" 0 "$mismatch"
for c in c1_status_mismatches c2_status_mismatches c3_unlock_below_100 c4_stations_over_capacity payments_left_after_delete; do
  expect_eq "consistency $c" 0 "$(grep -A1 "^$c\$" results/checks.txt | tail -1)"
done
expect_eq "DECIMAL(4,2) stores 12.345 as" 12.35 "$(grep -A1 '^p6_stored_pct$' results/checks.txt | tail -1)"
expect_eq "same-second rides finished" 1 "$(grep -A1 '^p7_same_second_rides$' results/checks.txt | tail -1)"
expect_eq "charge of a 3-day ride, kopecks" -2247400 "$(grep -A1 '^p8_long_ride_charge$' results/checks.txt | tail -1)"
dbroot -e "DROP DATABASE micromobility_check;" 2>/dev/null
db -t < sql/queries.sql > results/queries.txt 2>&1
expect_eq "sql/queries.sql ran without errors" 0 "$(grep -c '^ERROR' results/queries.txt)"
part_end "part 1"

# ---------------------------------------------------------------- part 2: CRUD and rules through the HTTP forms
part part2-crud "part 2: CRUD and business rules through the HTTP forms"
ST_FULL=$(q "SELECT id FROM stations WHERE name = 'Arsenalna'")
ST_KHR=$(q "SELECT id FROM stations WHERE name = 'Khreshchatyk'")
ST_VDNH=$(q "SELECT id FROM stations WHERE name = 'VDNH'")
OP_BOLT=$(q "SELECT id FROM operators WHERE name = 'Bolt'")
OP_JET=$(q "SELECT id FROM operators WHERE name = 'Jet'")

say "-- operators"
submit /operators/save -d id= -d name=Testo --data-urlencode phone=+380441112233 -d email=hello@testo.example -d commissionPct=15.00
expect_eq "create operator: row exists with commission" "15.00" "$(q "SELECT commission_pct FROM operators WHERE name = 'Testo'")"
OP_TEST=$(q "SELECT id FROM operators WHERE name = 'Testo'")
submit /operators/save -d id="$OP_TEST" -d name=Testo --data-urlencode phone=+380441112233 -d email=hello@testo.example -d commissionPct=12.50
expect_eq "edit operator: commission changed" "12.50" "$(q "SELECT commission_pct FROM operators WHERE id = $OP_TEST")"
submit /operators/save -d id= -d name=Testo --data-urlencode phone=+380441112244 -d email=x@testo.example -d commissionPct=10
expect_page "create operator with a taken name is refused" "Оператор з такою назвою вже є"
submit /operators/save -d id= -d name=Bad -d phone=12345 -d email=bad -d commissionPct=70
expect_page "operator form validation" "Телефон має вигляд"
submit "/operators/$OP_BOLT/delete"
expect_page "delete an operator with tariffs refused" "Оператора не можна видалити"
submit "/operators/$OP_TEST/delete"
expect_eq "delete operator: row gone" 0 "$(q "SELECT COUNT(*) FROM operators WHERE id = $OP_TEST")"

say "-- tariffs"
submit /operators/save -d id= -d name=Tarifo --data-urlencode phone=+380441112255 -d email=t@tarifo.example -d commissionPct=10
OP_TAR=$(q "SELECT id FROM operators WHERE name = 'Tarifo'")
submit /tariffs/save -d id= -d operatorId="$OP_TAR" -d vehicleType=E_BIKE -d unlockFee=8.00 -d perMinute=3.50
expect_eq "create tariff: prices in kopecks" "800:350" "$(q "SELECT CONCAT(unlock_fee_kop, ':', per_minute_kop) FROM tariffs WHERE operator_id = $OP_TAR")"
TR_TEST=$(q "SELECT id FROM tariffs WHERE operator_id = $OP_TAR")
submit /tariffs/save -d id= -d operatorId="$OP_TAR" -d vehicleType=E_BIKE -d unlockFee=9 -d perMinute=4
expect_page "second tariff for the same operator and type refused" "тариф уже є"
submit /tariffs/save -d id="$TR_TEST" -d operatorId="$OP_TAR" -d vehicleType=E_BIKE -d unlockFee=8.00 -d perMinute=4.00
expect_eq "edit tariff: per-minute price" 400 "$(q "SELECT per_minute_kop FROM tariffs WHERE id = $TR_TEST")"
TR_BOLT=$(q "SELECT id FROM tariffs WHERE operator_id = $OP_BOLT AND vehicle_type = 1")
submit "/tariffs/$TR_BOLT/delete"
expect_page "delete a tariff in use refused" "Тариф не можна видалити"
submit /tariffs/save -d id="$TR_BOLT" -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d unlockFee=10.00 -d perMinute=5.30
expect_eq "edit a tariff that vehicles use (key unchanged): price changed" 530 "$(q "SELECT per_minute_kop FROM tariffs WHERE id = $TR_BOLT")"
submit /tariffs/save -d id="$TR_BOLT" -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d unlockFee=10.00 -d perMinute=5.20
expect_eq "tariff price changed back" 520 "$(q "SELECT per_minute_kop FROM tariffs WHERE id = $TR_BOLT")"
submit "/tariffs/$TR_TEST/delete"
expect_eq "delete tariff: row gone" 0 "$(q "SELECT COUNT(*) FROM tariffs WHERE id = $TR_TEST")"
submit "/operators/$OP_TAR/delete"
expect_eq "delete the helper operator" 0 "$(q "SELECT COUNT(*) FROM operators WHERE id = $OP_TAR")"

say "-- stations"
submit /stations/save -d id= -d name="Test Station" -d address="Test St 1" -d capacity=3
ST_TEST=$(q "SELECT id FROM stations WHERE name = 'Test Station'")
expect_eq "create station: capacity" 3 "$(q "SELECT capacity FROM stations WHERE id = $ST_TEST")"
submit /stations/save -d id="$ST_TEST" -d name="Test Station" -d address="Test St 1" -d capacity=4
expect_eq "edit station: capacity" 4 "$(q "SELECT capacity FROM stations WHERE id = $ST_TEST")"
submit /stations/save -d id="$ST_KHR" -d name=Khreshchatyk -d address="Khreshchatyk St, 22" -d capacity=2
expect_page "capacity below the parked count refused" "Місткість не може бути меншою"
submit "/stations/$ST_KHR/delete"
expect_page "delete a station with vehicles refused" "Станцію не можна видалити"
submit "/stations/$ST_TEST/delete"
expect_eq "delete station: row gone" 0 "$(q "SELECT COUNT(*) FROM stations WHERE id = $ST_TEST")"

say "-- vehicles"
submit /vehicles/save -d id= -d code=TST-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Test model" -d batteryPct=70 -d status=AVAILABLE -d stationId="$ST_KHR"
expect_eq "create vehicle: status and station" "1:$ST_KHR" "$(q "SELECT CONCAT(status, ':', station_id) FROM vehicles WHERE code = 'TST-0001'")"
V_TEST=$(q "SELECT id FROM vehicles WHERE code = 'TST-0001'")
submit /vehicles/save -d id="$V_TEST" -d code=TST-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Test model 2" -d batteryPct=55 -d status=CHARGING -d stationId=
expect_eq "edit vehicle to CHARGING: station cleared" "3:NULL:55" "$(q "SELECT CONCAT(status, ':', IFNULL(station_id, 'NULL'), ':', battery_pct) FROM vehicles WHERE id = $V_TEST")"
submit /vehicles/save -d id="$V_TEST" -d code=TST-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Test model 2" -d batteryPct=55 -d status=AVAILABLE -d stationId=
expect_page "AVAILABLE without a station refused" "оберіть станцію"
submit /vehicles/save -d id= -d code=JTB-0001 -d operatorId="$OP_JET" -d vehicleType=E_BIKE -d model="No tariff" -d batteryPct=70 -d status=AVAILABLE -d stationId="$ST_KHR"
expect_page "vehicle of an operator without a tariff for the type refused" "немає тарифу"
submit /vehicles/save -d id= -d code=FUL-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Full" -d batteryPct=70 -d status=AVAILABLE -d stationId="$ST_FULL"
expect_page "vehicle at a full station refused" "заповнена"
V_RETIRED=$(q "SELECT id FROM vehicles WHERE code = 'ZEL-0001'")
submit "/vehicles/$V_RETIRED/delete"
expect_page "delete a vehicle with rides refused" "Транспорт не можна видалити"
submit "/vehicles/$V_TEST/delete"
expect_eq "delete vehicle: row gone" 0 "$(q "SELECT COUNT(*) FROM vehicles WHERE id = $V_TEST")"

say "-- customers"
submit /customers/save -d id= -d lastName=Тестенко -d firstName=Тарас --data-urlencode phone=+380670000001 -d email=
expect_eq "create customer: bonus row of 200 UAH exists" "1:20000" "$(q "SELECT CONCAT(COUNT(*), ':', SUM(p.amount_kop)) FROM payments p JOIN customers c ON c.id = p.customer_id WHERE c.phone = '+380670000001' AND p.kind = 1")"
C_DEL=$(q "SELECT id FROM customers WHERE phone = '+380670000001'")
submit /customers/save -d id="$C_DEL" -d lastName=Тестенко -d firstName=Остап --data-urlencode phone=+380670000001 -d email=ostap@example.com
expect_eq "edit customer: name and e-mail" "Остап:ostap@example.com" "$(q "SELECT CONCAT(first_name, ':', email) FROM customers WHERE id = $C_DEL")"
submit /customers/save -d id= -d lastName=Дубль -d firstName=Дмитро --data-urlencode phone=+380670000001 -d email=
expect_page "customer with a taken phone refused" "Клієнт з таким телефоном уже є"
submit "/customers/$C_DEL/delete"
expect_eq "delete a customer with only the bonus row: customer and bonus gone" "0:0" "$(q "SELECT CONCAT((SELECT COUNT(*) FROM customers WHERE id = $C_DEL), ':', (SELECT COUNT(*) FROM payments WHERE customer_id = $C_DEL))")"
submit /customers/save -d id= -d lastName=Поповнений -d firstName=Павло --data-urlencode phone=+380670000003 -d email=
C_TOP=$(q "SELECT id FROM customers WHERE phone = '+380670000003'")
submit /payments/topup -d customerId="$C_TOP" -d amount=100
submit "/customers/$C_TOP/delete"
expect_page "delete a customer with a top-up refused" "є платежі"
C_RIDES=$(q "SELECT customer_id FROM rides ORDER BY id LIMIT 1")
submit "/customers/$C_RIDES/delete"
expect_page "delete a customer with rides refused" "у нього є поїздки"

say "-- rides, payments, Diia"
submit /customers/save -d id= -d lastName=Райдер -d firstName=Ірина --data-urlencode phone=+380670000002 -d email=
C_R=$(q "SELECT id FROM customers WHERE phone = '+380670000002'")
submit /vehicles/save -d id= -d code=RID-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Ride one" -d batteryPct=80 -d status=AVAILABLE -d stationId="$ST_KHR"
submit /vehicles/save -d id= -d code=RID-0002 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Ride two" -d batteryPct=80 -d status=AVAILABLE -d stationId="$ST_KHR"
V1=$(q "SELECT id FROM vehicles WHERE code = 'RID-0001'")
V2=$(q "SELECT id FROM vehicles WHERE code = 'RID-0002'")
rides_before=$(q "SELECT COUNT(*) FROM rides")
submit /rides/start -d customerId="$C_R" -d vehicleId="$V1"
expect_page "unlock by an unverified customer refused" "не підтверджений через Дію"
submit "/customers/$C_R/verify"
expect_eq "verify through Diia: time is set" 1 "$(q "SELECT diia_verified_at IS NOT NULL FROM customers WHERE id = $C_R")"
submit "/customers/$C_R/verify"
expect_page "second verification refused" "вже підтверджено"
C_LOW=$(q "SELECT c.id FROM customers c WHERE c.diia_verified_at IS NOT NULL AND (SELECT COALESCE(SUM(amount_kop), 0) FROM payments WHERE customer_id = c.id) < 10000 AND NOT EXISTS (SELECT 1 FROM rides r WHERE r.customer_id = c.id AND r.ended_at IS NULL) ORDER BY c.id LIMIT 1")
submit /rides/start -d customerId="$C_LOW" -d vehicleId="$V1"
expect_page "unlock with a balance below 100 UAH refused" "для розблокування потрібно щонайменше 100,00 грн"
submit /payments/topup -d customerId="$C_R" -d amount=150
expect_page "top-up of 150 UAH refused (only 100, 200, 500)" "100, 200 або 500"
submit /payments/topup -d customerId="$C_R" -d amount=100
expect_eq "top-up once: balance is bonus 200 + 100 UAH" 30000 "$(q "SELECT SUM(amount_kop) FROM payments WHERE customer_id = $C_R")"
V_LOWBAT=$(q "SELECT id FROM vehicles WHERE code = 'BLT-0004'")
submit /rides/start -d customerId="$C_R" -d vehicleId="$V_LOWBAT"
expect_page "unlock with battery below 10 % refused" "Заряд транспорту 8 %"
for code in BLT-0014 BLT-0007 ZEL-0001; do
  vid=$(q "SELECT id FROM vehicles WHERE code = '$code'")
  st=$(q "SELECT status FROM vehicles WHERE id = $vid")
  submit /rides/start -d customerId="$C_R" -d vehicleId="$vid"
  expect_page "unlock of $code (status $st) refused" "Транспорт недоступний"
done
expect_eq "refused unlocks created no ride" "$rides_before" "$(q "SELECT COUNT(*) FROM rides")"

submit /rides/start -d customerId="$C_R" -d vehicleId="$V1"
RIDE_A=$(q "SELECT id FROM rides WHERE customer_id = $C_R AND ended_at IS NULL")
expect_eq "start ride: vehicle status IN_RIDE (2) and no station" "2:NULL" "$(q "SELECT CONCAT(status, ':', IFNULL(station_id, 'NULL')) FROM vehicles WHERE id = $V1")"
expect_eq "start ride: prices copied from the Bolt scooter tariff" "1000:520" "$(q "SELECT CONCAT(unlock_fee_kop, ':', per_minute_kop) FROM rides WHERE id = $RIDE_A")"
submit /rides/start -d customerId="$C_R" -d vehicleId="$V2"
expect_page "second active ride of the customer refused" "вже є активна поїздка"
submit /vehicles/save -d id="$V1" -d code=RID-0001 -d operatorId="$OP_BOLT" -d vehicleType=E_SCOOTER -d model="Ride one" -d batteryPct=80 -d status=CHARGING -d stationId=
expect_page "status change of a vehicle in a ride refused" "Транспорт у поїздці"
expect_eq "vehicle still IN_RIDE after the refused change" 2 "$(q "SELECT status FROM vehicles WHERE id = $V1")"
submit "/rides/$RIDE_A/finish" -d stationId="$ST_FULL"
expect_page "finish at a full station refused" "заповнена"
expect_eq "ride still active after the refused finish" 1 "$(q "SELECT ended_at IS NULL FROM rides WHERE id = $RIDE_A")"
submit "/rides/$RIDE_A/finish" -d stationId="$ST_VDNH"
expect_eq "finish: vehicle AVAILABLE (1) at the chosen station" "1:$ST_VDNH" "$(q "SELECT CONCAT(status, ':', station_id) FROM vehicles WHERE id = $V1")"
expect_eq "finish: charge 10.00 + 5.20 for one started minute, kopecks" -1520 "$(q "SELECT amount_kop FROM payments WHERE ride_id = $RIDE_A AND kind = 3")"
expect_eq "finish: stored commission 10 % of 1520" 152 "$(q "SELECT commission_kop FROM rides WHERE id = $RIDE_A")"
expect_eq "finish: charge is dated at the finish time" 1 "$(q "SELECT p.created_at = r.ended_at FROM payments p JOIN rides r ON r.id = p.ride_id WHERE r.id = $RIDE_A AND p.kind = 3")"

# one ride started and finished in the same second (the period CHECK allows equal times)
same=0
for _ in 1 2 3 4 5 6 7 8; do
  while [ $((10#$(date +%N) / 1000000)) -gt 100 ]; do sleep 0.05; done
  submit /rides/start -d customerId="$C_R" -d vehicleId="$V2"
  RIDE_B=$(q "SELECT id FROM rides WHERE customer_id = $C_R AND ended_at IS NULL")
  submit "/rides/$RIDE_B/finish" -d stationId="$ST_KHR"
  if [ "$(q "SELECT started_at = ended_at FROM rides WHERE id = $RIDE_B")" = 1 ]; then same=1; break; fi
done
expect_eq "a ride finished in the same second as its start was accepted" 1 "$same"
if [ "$same" = 1 ]; then
  expect_eq "same-second ride is billed as one minute" -1520 "$(q "SELECT amount_kop FROM payments WHERE ride_id = $RIDE_B AND kind = 3")"
fi

submit /rides/start -d customerId="$C_R" -d vehicleId="$V2"
RIDE_C=$(q "SELECT id FROM rides WHERE customer_id = $C_R AND ended_at IS NULL")
submit "/rides/$RIDE_C/cancel" -d stationId="$ST_KHR"
expect_eq "cancel active ride: ride and payments gone, vehicle AVAILABLE at the station" "0:0:1:$ST_KHR" "$(q "SELECT CONCAT((SELECT COUNT(*) FROM rides WHERE id = $RIDE_C), ':', (SELECT COUNT(*) FROM payments WHERE ride_id = $RIDE_C), ':', status, ':', station_id) FROM vehicles WHERE id = $V2")"
submit "/rides/$RIDE_A/finish" -d stationId="$ST_KHR"
expect_page "second finish of a finished ride refused" "Поїздку вже завершено"
submit "/rides/$RIDE_A/cancel" -d stationId="$ST_KHR"
expect_page "cancel of a finished ride refused" "Завершену поїздку скасувати не можна"
submit "/rides/$RIDE_A"
expect_eq "edit of a finished ride: no update path (POST on the ride is 405)" 405 "$STATUS"

submit "/rides/$RIDE_A/refund" -d amount=100.00
expect_page "refund above the charge refused" "Сума повернення має бути"
submit "/rides/$RIDE_A/refund" -d amount=5.00
expect_eq "refund: row of kind 4 with 500 kopecks" 500 "$(q "SELECT amount_kop FROM payments WHERE ride_id = $RIDE_A AND kind = 4")"
submit "/rides/$RIDE_A/refund" -d amount=1.00
expect_page "second refund of the same ride refused" "повернення вже було"
expect_eq "refund leaves the stored commission unchanged" 152 "$(q "SELECT commission_kop FROM rides WHERE id = $RIDE_A")"
submit "/rides/$RIDE_C/refund" -d amount=1.00
expect_eq "refund of a ride that does not exist is refused (no payment created)" 0 "$(q "SELECT COUNT(*) FROM payments WHERE ride_id = $RIDE_C")"

expect_eq "BR21: vehicles whose status 2 disagrees with an active ride" 0 "$(q "SELECT COUNT(*) FROM vehicles v WHERE (v.status = 2) <> EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL)")"
expect_eq "BR22: stations over capacity" 0 "$(q "SELECT COUNT(*) FROM (SELECT s.id FROM stations s LEFT JOIN vehicles v ON v.station_id = s.id GROUP BY s.id, s.capacity HAVING COUNT(v.id) > s.capacity) t")"
say "-- every page renders"
RIDE_ACTIVE=$(q "SELECT id FROM rides WHERE ended_at IS NULL ORDER BY id LIMIT 1")
for p in / /operators /operators/new /operators/"$OP_BOLT"/edit /tariffs /tariffs/new /tariffs/"$TR_BOLT"/edit /stations /stations/new \
         /stations/"$ST_KHR"/edit /vehicles /vehicles/new /vehicles/"$V1"/edit /customers /customers/new /customers/"$C_R"/edit \
         /rides /rides/start /rides/"$RIDE_A" /rides/"$RIDE_ACTIVE" /payments "/payments?customerId=$C_R" /reports \
         /reports/r1 /reports/r3 /reports/r4 /reports/r5 /reports/r6 /reports/r7; do
  expect_eq "GET $p" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE$p")"
done
part_end "part 2"

# ---------------------------------------------------------------- part 3: report pages against the SQL
part part3-reports "part 3: every report page equals its SQL in sql/queries.sql (the whole ordered result, not only the row count)"
# Each data row of a page carries data-v="v1|v2|..." (raw values: kopecks, ids, counts). The SQL result is cut to the same
# columns and joined with |. Equal lists mean equal rows, values and order, incl. the R5 total row.
block() { awk -v n="$1" '/^-- R[0-9]\./ {cur = substr($2, 2, 1)} /^-- Seed check/ {cur = ""} cur == n' sql/queries.sql | grep -v '^SET '; }
sql_values() {  # report number, SET statement, awk columns
  { echo "$2"; block "$1"; } | db -N -B 2>/dev/null \
    | awk -F'\t' -v cols="$3" '{n = split(cols, c, ","); s = ""; for (i = 1; i <= n; i++) s = s (i > 1 ? "|" : "") $c[i]; print s}'
}
page_values() { curl -s -G "$BASE$1" "${@:2}" | grep -o 'data-v="[^"]*"' | sed -e 's/^data-v="//' -e 's/"$//'; }
compare() {  # name, report number, SET statement, awk columns, page path, curl -G arguments...
  local name=$1 n=$2 set=$3 cols=$4 path=$5; shift 5
  page_values "$path" "$@" > "$TMP/page.txt"
  sql_values "$n" "$set" "$cols" > "$TMP/sql.txt"
  local rows
  rows=$(wc -l < "$TMP/sql.txt")
  if cmp -s "$TMP/page.txt" "$TMP/sql.txt" && [ "$rows" -gt 0 ]; then ok "$name: $rows rows, values and order equal"; else bad "$name: page and SQL differ ($(wc -l < "$TMP/page.txt") page rows, $rows SQL rows)"; fi
}
NOF="@from = NULL, @to = NULL"
compare "R1 no filters" 1 "SET @op = NULL, @type = NULL, @station = NULL, @min_battery = NULL;" 1,9,6 /reports/r1
compare "R1 scooters, battery 20 or more" 1 "SET @op = NULL, @type = 1, @station = NULL, @min_battery = 20;" 1,9,6 /reports/r1 --data-urlencode type=E_SCOOTER --data-urlencode minBattery=20
# vehicles list, battery filter: one case per comparison against plain SQL (page: code|battery, ordered by code)
compare_sql() {  # name, SQL query returning code and battery_pct, page path, curl -G arguments...
  local name=$1 query=$2 path=$3; shift 3
  page_values "$path" "$@" > "$TMP/page.txt"
  db -N -B -e "$query" 2>/dev/null | awk -F'\t' '{print $1 "|" $2}' > "$TMP/sql.txt"
  local rows
  rows=$(wc -l < "$TMP/sql.txt")
  if cmp -s "$TMP/page.txt" "$TMP/sql.txt" && [ "$rows" -gt 0 ]; then ok "$name: $rows rows, values and order equal"; else bad "$name: page and SQL differ ($(wc -l < "$TMP/page.txt") page rows, $rows SQL rows)"; fi
}
compare_sql "vehicles, battery any" "SELECT code, battery_pct FROM vehicles ORDER BY code" /vehicles
compare_sql "vehicles, battery more than 50" "SELECT code, battery_pct FROM vehicles WHERE battery_pct > 50 ORDER BY code" /vehicles --data-urlencode batteryCmp=GT --data-urlencode battery=50
compare_sql "vehicles, battery less than 30" "SELECT code, battery_pct FROM vehicles WHERE battery_pct < 30 ORDER BY code" /vehicles --data-urlencode batteryCmp=LT --data-urlencode battery=30
EQV=$(q "SELECT battery_pct FROM vehicles GROUP BY battery_pct ORDER BY COUNT(*) DESC, battery_pct LIMIT 1")
compare_sql "vehicles, battery equal to $EQV" "SELECT code, battery_pct FROM vehicles WHERE battery_pct = $EQV ORDER BY code" /vehicles --data-urlencode batteryCmp=EQ --data-urlencode battery="$EQV"
expect_eq "vehicles, battery less than 0 is an empty list" 0 "$(page_values /vehicles --data-urlencode batteryCmp=LT --data-urlencode battery=0 | wc -l)"
compare "R2 no filters" 2 "SET $NOF, @op = NULL, @type = NULL, @q = NULL, @status = NULL;" 1,8,9,10,11 /rides
compare "R2 surname prefix Ко" 2 "SET $NOF, @op = NULL, @type = NULL, @q = 'Ко', @status = NULL;" 1,8,9,10,11 /rides --data-urlencode q=Ко
compare "R2 finished rides" 2 "SET $NOF, @op = NULL, @type = NULL, @q = NULL, @status = 'FINISHED';" 1,8,9,10,11 /rides --data-urlencode status=finished
# date filters: Kyiv days, "to" inclusive, UTC bounds in the SQL (+3 until 25.10, +2 after). Kyiv 09.10 01:22 is UTC 08.10 22:22.
compare "R2 Kyiv days 09.10 to 14.10 (UTC bounds 08.10 21:00 to 14.10 21:00)" 2 "SET @from = '2026-10-08 21:00:00', @to = '2026-10-14 21:00:00', @op = NULL, @type = NULL, @q = NULL, @status = NULL;" 1,8,9,10,11 /rides --data-urlencode from=2026-10-09 --data-urlencode to=2026-10-14
expect_eq "R2 date filter includes ride 5 (starts 01:22 Kyiv on 09.10, 22:22 UTC on 08.10)" 1 "$(page_values /rides --data-urlencode from=2026-10-09 --data-urlencode to=2026-10-13 | grep -c '^5|')"
compare "R2 Kyiv days 01.11 to 30.11" 2 "SET @from = '2026-10-31 22:00:00', @to = '2026-11-30 22:00:00', @op = NULL, @type = NULL, @q = NULL, @status = NULL;" 1,8,9,10,11 /rides --data-urlencode from=2026-11-01 --data-urlencode to=2026-11-30
compare "R3 no filters (months in Kyiv time)" 3 "SET $NOF;" 1,2,3,4,5,6,7 /reports/r3
compare "R3 Kyiv days 01.10 to 30.11" 3 "SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00';" 1,2,3,4,5,6,7 /reports/r3 --data-urlencode from=2026-10-01 --data-urlencode to=2026-11-30
compare "R3 Kyiv days 01.11 to 30.11" 3 "SET @from = '2026-10-31 22:00:00', @to = '2026-11-30 22:00:00';" 1,2,3,4,5,6,7 /reports/r3 --data-urlencode from=2026-11-01 --data-urlencode to=2026-11-30
compare "R4 no filters" 4 "SET $NOF, @min_rides = NULL;" 1,3,4,5 /reports/r4
compare "R4 at least 3 rides" 4 "SET $NOF, @min_rides = 3;" 1,3,4,5 /reports/r4 --data-urlencode minRides=3
compare "R4 Kyiv days 01.10 to 30.11, at least 3 rides" 4 "SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00', @min_rides = 3;" 1,3,4,5 /reports/r4 --data-urlencode from=2026-10-01 --data-urlencode to=2026-11-30 --data-urlencode minRides=3
compare "R5 no filters (incl. the total row)" 5 "SET $NOF;" 1,2,3,4,5,6,7 /reports/r5
compare "R5 Kyiv days 01.10 to 30.11" 5 "SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00';" 1,2,3,4,5,6,7 /reports/r5 --data-urlencode from=2026-10-01 --data-urlencode to=2026-11-30
expect_eq "R5 last row is the total row" "NULL|1" "$(page_values /reports/r5 | tail -1 | cut -d'|' -f1,2)"
compare "R6 default threshold 100 UAH" 6 "SET @threshold = 10000;" 1,4,5 /reports/r6
compare "R7 default level 20 %" 7 "SET @level = 20;" 1,2,3 /reports/r7
expect_eq "search text with a % is a plain character (no rides match)" 0 "$(page_values /rides --data-urlencode q=% | wc -l)"
expect_eq "unknown id gives a plain 404 page" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/operators/999999/edit")"
part_end "part 3"

echo
echo "summary: $PASS ok, $FAIL failed"
[ "$FAIL" -eq 0 ]
