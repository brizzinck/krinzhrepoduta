#!/usr/bin/env bash
# Показ курсової в терміналі: стек, таблиці, обмеження БД, правила через застосунок, звіти R1-R7, адреса адмінки.
#   ./show.sh            усі кроки по черзі (на свіжому стеку; make show спершу робить reset)
#   ./show.sh <крок>     один крок: stack, tables, checks, rules, reports, admin
# Нічого не пише в теку проєкту: тимчасові файли лежать у mktemp, обмеження перевіряються в окремій базі
# micromobility_scratch, яка видаляється в кінці. Потрібен лише Docker і запущений стек (make up).
set -u
cd "$(dirname "$0")"
BASE="http://127.0.0.1:${APP_PORT:-18480}"
TMP=$(mktemp -d)
JAR="$TMP/cookies"
SCRATCH=micromobility_scratch
trap 'rm -rf "$TMP"' EXIT

# MySQL у контейнері db: від користувача застосунку і від root (додаткові аргументи йдуть у mysql)
db() { docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" --default-character-set=utf8mb4 "$MYSQL_DATABASE" "$@"' sh "$@"; }
dbroot() { docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 "$@"' sh "$@"; }
q() { db -N -B -e "$1" 2>/dev/null | head -1; }

step() { echo; echo "Крок $1. $2"; }
expect() { echo "Очікувано: $*"; }
# друкує запит як у клієнті mysql і виконує його у форматі таблиці
run_sql() {
  printf '%s\n' "$1" | sed '1s/^/mysql> /;2,$s/^/       /'
  db -t -e "$1" 2>&1
}

need_stack() {
  if [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/")" != 200 ]; then
    echo "Стек не запущено: адмінка не відповідає на $BASE/. Запустіть make up (або make show: він сам робить reset)." >&2
    exit 1
  fi
}

# POST форми, перехід за редиректом з cookie (повідомлення живуть у сесії). Задає STATUS і PAGE.
submit() {
  local path=$1; shift
  local hdr loc
  hdr=$(curl -s -D - -o "$TMP/page" -c "$JAR" -b "$JAR" -X POST "$@" "$BASE$path")
  STATUS=$(printf '%s' "$hdr" | head -1 | awk '{print $2}')
  loc=$(printf '%s' "$hdr" | tr -d '\r' | awk 'tolower($1)=="location:" {print $2}')
  if [ -n "$loc" ]; then
    case "$loc" in /*) loc="$BASE$loc";; esac
    curl -s -c "$JAR" -b "$JAR" -o "$TMP/page" "$loc"
  fi
  PAGE=$(cat "$TMP/page")
}
# повідомлення застосунку зі сторінки (зелене або червоне)
flash() {
  printf '%s' "$PAGE" | grep -o 'id="flash-[a-z]*">[^<]*' | head -1 \
    | sed -e 's/^id="flash-error">/відмова: /' -e 's/^id="flash-ok">/успіх: /' -e 's/^id="flash-[a-z]*">/повідомлення: /' \
          -e 's/&quot;/"/g' -e 's/&amp;/\&/g' -e "s/&#39;/'/g"
}
# post <шлях> [ключ=значення ...]: показує запит і відповідь застосунку
post() {
  local path=$1; shift
  local args=() kv
  for kv in "$@"; do args+=(-d "$kv"); done
  echo "POST $path${*:+ $*}"
  submit "$path" "${args[@]}"
  echo "  відповідь застосунку, $(flash)"
}

# ---------------------------------------------------------------- крок 1
show_stack() {
  need_stack
  step 1 "Стек у Docker і міграції Flyway"
  expect "два контейнери (db healthy, app running); Flyway застосував версії 1 і 2, обидві success = 1; Hibernate звірив схему без помилок"
  echo "docker compose ps"
  docker compose ps --format 'table {{.Service}}\t{{.Image}}\t{{.State}}\t{{if .Health}}{{.Health}}{{else}}-{{end}}\t{{.Ports}}'
  echo
  run_sql "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
  echo
  echo "Журнал застосунку (docker compose logs app):"
  docker compose logs app 2>&1 | grep -E 'Successfully applied|Started MicromobilityApplication' \
    | sed -E -e 's/^.*(Started MicromobilityApplication) in .*/\1/' -e 's/^.*DbMigrate +: //' -e 's/ \(execution time [0-9:.]+s\)//'
  echo "SchemaManagementException у журналі: $(docker compose logs app 2>&1 | grep -c SchemaManagementException) (ddl-auto=validate лише звіряє схему з сутностями)"
  echo "GET $BASE/: HTTP $(curl -s -o /dev/null -w '%{http_code}' "$BASE/")"
}

# ---------------------------------------------------------------- крок 2
show_tables() {
  need_stack
  step 2 "Сім таблиць предметної області і кількість рядків (дані з V2__seed.sql)"
  expect "operators 3, tariffs 5, stations 6, vehicles 38, customers 12, rides 57, payments 89 на свіжому стеку"
  run_sql "SELECT t.table_name AS tbl, (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_schema = t.table_schema AND c.table_name = t.table_name) AS cols, CASE t.table_name
  WHEN 'operators' THEN (SELECT COUNT(*) FROM operators) WHEN 'tariffs' THEN (SELECT COUNT(*) FROM tariffs)
  WHEN 'stations' THEN (SELECT COUNT(*) FROM stations) WHEN 'vehicles' THEN (SELECT COUNT(*) FROM vehicles)
  WHEN 'customers' THEN (SELECT COUNT(*) FROM customers) WHEN 'rides' THEN (SELECT COUNT(*) FROM rides)
  WHEN 'payments' THEN (SELECT COUNT(*) FROM payments) END AS row_count
FROM information_schema.tables t
WHERE t.table_schema = 'micromobility' AND t.table_name <> 'flyway_schema_history'
ORDER BY FIELD(t.table_name, 'operators', 'tariffs', 'stations', 'vehicles', 'customers', 'rides', 'payments');"
  echo
  echo "Обмеження, які тримає сама БД (не код застосунку):"
  run_sql "SELECT constraint_type, COUNT(*) AS n FROM information_schema.table_constraints
WHERE table_schema = 'micromobility' AND table_name <> 'flyway_schema_history'
GROUP BY constraint_type ORDER BY n DESC;"
}

# ---------------------------------------------------------------- крок 3
run_case() {  # код випадку з sql/checks.sql, очікуване обмеження, пояснення
  local k=$1 name=$2 desc=$3 stmt
  stmt=$(awk -v k="-- $k expect" 'index($0, k) == 1 {on = 1; next} on && (/^--/ || /^$/) {exit} on {print}' sql/checks.sql)
  echo
  echo "$k: $desc. Очікувано: обмеження $name"
  printf '%s\n' "$stmt" | sed '1s/^/mysql> /;2,$s/^/       /'
  { printf '%s\n' "$PRE"; printf '%s\n' "$stmt"; } | dbroot --force "$SCRATCH" 2>&1 | grep '^ERROR' | sed -E 's/ at line [0-9]+//'
}

show_checks() {
  need_stack
  step 3 "Обмеження БД: недопустимі дані відхиляє сама СУБД"
  expect "кожен оператор нижче закінчується рядком ERROR з назвою обмеження; повний набір sql/checks.sql: 45 з 45 відхилено потрібним обмеженням"
  echo "Перевірка йде в окремій базі $SCRATCH: схема V1 і дані V2 завантажуються заново, база застосунку не змінюється."
  dbroot -e "DROP DATABASE IF EXISTS $SCRATCH; CREATE DATABASE $SCRATCH;" 2>/dev/null
  dbroot "$SCRATCH" < app/src/main/resources/db/migration/V1__schema.sql 2>/dev/null
  dbroot "$SCRATCH" < app/src/main/resources/db/migration/V2__seed.sql 2>/dev/null
  # початок sql/checks.sql до першої перевірки: транзакція і змінні з номерами з seed
  PRE=$(awk 'index($0, "-- C1 expect") == 1 {exit} {print}' sql/checks.sql)
  run_case N1 chk_operators_phone "телефон оператора не за шаблоном +380XXXXXXXXX"
  run_case N3 uq_operators_name "назва оператора унікальна без урахування регістру: bolt і Bolt"
  run_case N7 fk_tariffs_operator "тариф для оператора, якого немає"
  run_case N10 chk_vehicles_battery "заряд транспорту більше 100 відсотків"
  run_case N19 ux_rides_active_customer "у клієнта вже є активна поїздка"
  run_case N24 uq_payments_ride_kind "другий платіж-списання за ту саму поїздку"
  run_case N26 fk_tariffs_operator "видалення оператора з тарифами (RESTRICT, без каскаду)"
  run_case N31 chk_rides_commission "комісія більша за вартість поїздки"
  run_case N41 chk_vehicles_station "вільний транспорт без станції"
  run_case N44 fk_vehicles_tariff "електровелосипед Jet, а тарифу Jet на велосипед немає"
  # увесь sql/checks.sql: кожен негативний оператор має впасти саме на своєму обмеженні
  dbroot --force "$SCRATCH" < sql/checks.sql > "$TMP/checks.txt" 2>&1
  local total errors mismatch
  total=$(grep -c '^-- N[0-9]* expect error' sql/checks.sql)
  errors=$(grep -c '^ERROR' "$TMP/checks.txt")
  grep -o '^-- N[0-9]* expect error [a-z_0-9]*' sql/checks.sql | awk '{print $2, $5}' > "$TMP/expected.txt"
  grep '^ERROR' "$TMP/checks.txt" | sed -E \
    -e "s/.*Check constraint '([a-z_0-9]+)'.*/\1/" \
    -e "s/.*for key '[a-z_]+\.([a-z_0-9]+)'.*/\1/" \
    -e 's/.*CONSTRAINT `([a-z_0-9]+)`.*/\1/' \
    -e 's/^ERROR ([0-9]+) .*/\1/' > "$TMP/actual.txt"
  mismatch=$(paste -d' ' "$TMP/expected.txt" "$TMP/actual.txt" | awk '$2 != $3 {n++} END {print n + 0}')
  echo
  echo "Увесь sql/checks.sql: негативних операторів $total, відхилено $errors, відхилено іншим обмеженням, ніж очікувалось: $mismatch."
  echo "Перевірки узгодженості (мають бути 0): c1_status_mismatches $(grep -A1 '^c1_status_mismatches$' "$TMP/checks.txt" | tail -1), c3_unlock_below_100 $(grep -A1 '^c3_unlock_below_100$' "$TMP/checks.txt" | tail -1), c4_stations_over_capacity $(grep -A1 '^c4_stations_over_capacity$' "$TMP/checks.txt" | tail -1)."
  dbroot -e "DROP DATABASE $SCRATCH;" 2>/dev/null
}

# ---------------------------------------------------------------- кроки 4-6
show_rules() {
  need_stack
  local C V_OK V_LOW V_2 ST_KHR ST_FULL RIDE before
  C=$(q "SELECT id FROM customers WHERE phone = '+380990000012'")
  if [ "$(q "SELECT diia_verified_at IS NULL AND (SELECT COUNT(*) FROM rides WHERE customer_id = $C) = 0 FROM customers WHERE id = $C")" != 1 ]; then
    echo "Кроки 4-6 потребують свіжого стеку (клієнт Остапенко ще не підтверджений і без поїздок). Запустіть make reset." >&2
    exit 1
  fi
  V_OK=$(q "SELECT id FROM vehicles WHERE code = 'BLT-0002'")
  V_LOW=$(q "SELECT id FROM vehicles WHERE code = 'BLT-0004'")
  V_2=$(q "SELECT id FROM vehicles WHERE code = 'BLT-0008'")
  ST_KHR=$(q "SELECT id FROM stations WHERE name = 'Khreshchatyk'")
  ST_FULL=$(q "SELECT id FROM stations WHERE name = 'Arsenalna'")
  before=$(q "SELECT COUNT(*) FROM rides")

  step 4 "Правила бізнесу через застосунок (HTTP, ті самі форми, що в браузері): відмови до початку поїздки"
  expect "клієнт без підтвердження Дії не розблокує транспорт; після підтвердження він має бонус 200 грн; транспорт із зарядом 8 % не розблоковується; поїздок у базі лишається $before"
  echo "Клієнт: Остапенко Богдан (id $C). Транспорт: BLT-0002 (id $V_OK, заряд 85 %), BLT-0004 (id $V_LOW, заряд 8 %). Суми в копійках."
  echo
  post /rides/start customerId="$C" vehicleId="$V_OK"
  echo "  поїздок у базі: $(q "SELECT COUNT(*) FROM rides")"
  echo
  post "/customers/$C/verify"
  run_sql "SELECT c.last_name, c.diia_verified_at IS NOT NULL AS verified, p.kind, p.amount_kop FROM customers c JOIN payments p ON p.customer_id = c.id WHERE c.id = $C;"
  echo
  post /rides/start customerId="$C" vehicleId="$V_LOW"
  echo "  поїздок у базі: $(q "SELECT COUNT(*) FROM rides")"

  step 5 "Поїздка від розблокування до оплати і відмови під час поїздки"
  expect "поїздка стартує, тариф копіюється в неї (1000 + 520 за хвилину), транспорт у статусі 2 без станції; друга поїздка клієнта і завершення на заповнену станцію відхилені; після завершення платіж -1520, комісія 152, транспорт знову вільний на станції"
  post /rides/start customerId="$C" vehicleId="$V_OK"
  RIDE=$(q "SELECT id FROM rides WHERE customer_id = $C AND ended_at IS NULL")
  run_sql "SELECT r.id AS ride_id, r.unlock_fee_kop, r.per_minute_kop, r.ended_at IS NULL AS active, v.status AS vehicle_status, v.station_id FROM rides r JOIN vehicles v ON v.id = r.vehicle_id WHERE r.id = $RIDE;"
  echo
  post /rides/start customerId="$C" vehicleId="$V_2"
  echo
  post "/rides/$RIDE/finish" stationId="$ST_FULL"
  echo
  post "/rides/$RIDE/finish" stationId="$ST_KHR"
  run_sql "SELECT r.id AS ride_id, p.kind, p.amount_kop, r.commission_kop, v.status AS vehicle_status, s.name AS station FROM rides r JOIN payments p ON p.ride_id = r.id AND p.kind = 3 JOIN vehicles v ON v.id = r.vehicle_id JOIN stations s ON s.id = v.station_id WHERE r.id = $RIDE;"

  step 6 "Повернення коштів і журнал платежів"
  expect "повторне завершення, повернення більше за списання і друге повернення відхилені; повернення 5,00 грн записане новим рядком (kind 4); баланс клієнта 18980 копійок = 20000 - 1520 + 500"
  post "/rides/$RIDE/finish" stationId="$ST_KHR"
  echo
  post "/rides/$RIDE/refund" amount=100.00
  echo
  post "/rides/$RIDE/refund" amount=5.00
  echo
  post "/rides/$RIDE/refund" amount=1.00
  echo
  echo "Журнал платежів клієнта (повернення це новий рядок, старі не змінюються):"
  run_sql "SELECT p.id, p.kind, p.amount_kop, p.ride_id FROM payments p WHERE p.customer_id = $C ORDER BY p.id;"
  echo
  run_sql "SELECT SUM(amount_kop) AS balance_kop FROM payments WHERE customer_id = $C;"
}

# ---------------------------------------------------------------- крок 7
# запит звіту n з sql/queries.sql без коментарів; для R1 параметри звужені, щоб таблиця була короткою
report_sql() {
  awk -v n="$1" '/^-- R[0-9]\./ {cur = substr($2, 2, 1)} /^-- Seed check/ {cur = ""} cur == n' sql/queries.sql | grep -v -e '^--' -e '^$'
}
report() {  # номер, назва, очікувано
  local sqltext
  sqltext=$(report_sql "$1")
  if [ "$1" = 1 ]; then
    sqltext=$(printf '%s\n' "$sqltext" | sed "s/^SET .*/SET @op = NULL, @type = 1, @station = (SELECT id FROM stations WHERE name = 'Khreshchatyk'), @min_battery = 50;/")
  fi
  echo
  echo "R$1. $2"
  echo "Очікувано: $3"
  printf '%s\n' "$sqltext"
  printf '%s\n' "$sqltext" | db -t 2>&1
}

show_reports() {
  need_stack
  step 7 "Звіти R1-R7: SQL з sql/queries.sql і результат (кошти в копійках, коди статусів і типів числом)"
  echo "Тип транспорту: 1 електросамокат, 2 електровелосипед. Статус: 1 вільний, 2 у поїздці, 3 заряджається, 4 ремонт, 5 списаний."
  echo "NULL у параметрі фільтра означає без фільтра. R3 і R5 вже враховують поїздку 58 з кроків 5-6 (Bolt, одне повернення 500 копійок)."
  echo "Дати в запитах задані як UTC-межі київських днів, тому на цій сторінці поїздка 58 видна, поки не настало 1 грудня."
  report 1 "вільний транспорт для розблокування (JOIN чотирьох таблиць, спершу найдешевша поїздка на 10 хвилин)" "чотири самокати на Khreshchatyk із зарядом від 50, спершу найдешевші (BLT-0002 і BLT-0008 Bolt, потім Jet і Zelektra)"
  report 2 "пошук поїздок: період, оператор, тип, клієнт, статус (усі фільтри необов'язкові)" "поїздки клієнтів на Ко... у листопаді: три поїздки Ковальчук Софії на Zelektra"
  report 3 "виручка, повернення і комісія за місяць завершення і оператора (GROUP BY за київським місяцем)" "шість рядків: три оператори за 2026-10 і за 2026-11; у 2026-11 Bolt шість поїздок, charged_kop 57320 і refunded_kop 500 (без кроків 5-6 було б 5 і 55800)"
  report 4 "п'ять клієнтів із найбільшими витратами (списання мінус повернення), не менше 3 поїздок" "п'ять рядків, першою Захарченко Марія (8 поїздок); Остапенко має одну поїздку, тому в список не входить"
  report 5 "зведена таблиця: оператори в рядках, типи транспорту в стовпцях, підсумок WITH ROLLUP" "рядки Bolt, Jet, Zelektra і підсумок (operator NULL, is_total 1): 56 поїздок, з них 49 самокатами (з поїздкою 58 у Bolt 9 самокатів і 12 поїздок)"
  report 6 "баланси гаманців нижче 100 грн (скласти платежі клієнта)" "шість клієнтів із балансом нижче 10000 копійок, першими двоє з від'ємним балансом; Остапенко (18980) не входить"
  report 7 "транспорт, який треба зарядити (вільний, заряд менше 20 %)" "два рядки: BLT-0004 (8) і ZEL-0008 (13)"
}

# ---------------------------------------------------------------- крок 8
show_admin() {
  need_stack
  step 8 "Адмінка застосунку"
  expect "усі сторінки меню відповідають HTTP 200; ту саму адресу відкрийте в браузері"
  local p
  for p in / /operators /tariffs /stations /vehicles /customers /rides /payments /reports /reports/r3; do
    printf '%s  %s\n' "$(curl -s -o /dev/null -w '%{http_code}' "$BASE$p")" "$BASE$p"
  done
  echo
  echo "Відкрийте в браузері: $BASE/"
  echo "У меню: Оператори, Тарифи, Станції, Транспорт, Клієнти, Поїздки, Платежі, Звіти. Поїздка клієнта Остапенко видна в розділі Поїздки."
}

show_all() {
  show_stack
  show_tables
  show_checks
  show_rules
  show_reports
  show_admin
  echo
  echo "Готово."
}

case "${1:-all}" in
  all) show_all ;;
  stack) show_stack ;;
  tables) show_tables ;;
  checks) show_checks ;;
  rules) show_rules ;;
  reports) show_reports ;;
  admin) show_admin ;;
  *) echo "використання: $0 [all|stack|tables|checks|rules|reports|admin]" >&2; exit 2 ;;
esac
