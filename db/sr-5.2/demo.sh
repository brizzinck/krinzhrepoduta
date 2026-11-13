#!/bin/sh
# Демонстрація логування й моніторингу MySQL 8.4 на базі car_rental у контейнері duikt-mysql.
# Використання: ./demo.sh slow | lock | binlog | errlog | all
# Змінюються лише змінні рівня сервера (SET GLOBAL), вони повертаються в кінці кожної частини.
# Дані car_rental залишаються без змін: тестові оновлення скасовуються або повертаються.
cd "$(dirname "$0")"
M=../_shared/mysql.sh

# друкує команду як у терміналі (без шляху до _shared) і виконує її
show() { printf '$ %s\n' "$(printf '%s' "$*" | sed 's#\.\./_shared/##g')"; sh -c "$*" 2>&1; }
# те саме, але друкує скорочений запис команди ("…" на місці пропущеного), а виконує повну
showd() { printf '$ %s\n' "$1"; shift; sh -c "$*" 2>&1; }

slow() {
  # повільний журнал у таблицю mysql.slow_log, поріг 0: фіксується кожен запит
  showd "mysql.sh -e \"SET GLOBAL long_query_time = 0; SET GLOBAL slow_query_log = ON; …\"" \
    "$M -e \"SET GLOBAL log_output = 'TABLE'; SET GLOBAL long_query_time = 0; SET GLOBAL slow_query_log = ON; TRUNCATE mysql.slow_log; TRUNCATE performance_schema.events_statements_summary_by_digest;\""
  for i in 1 2 3; do $M car_rental < demo.sql >/dev/null; done
  printf '$ %s\n' "mysql.sh car_rental < demo.sql   # тричі"
  showd "mysql.sh -t -e \"SELECT query_time, rows_sent, rows_examined, … FROM mysql.slow_log WHERE …\"" \
    "$M -t -e \"SELECT query_time, rows_sent, rows_examined, LEFT(sql_text, 20) AS sql_text FROM mysql.slow_log WHERE db = 'car_rental' AND sql_text LIKE 'SELECT r.rental_id%'\""
  showd "mysql.sh -t -e \"SELECT … AS avg_ms, … FROM performance_schema.events_statements_summary_by_digest …\"" \
    "$M -t -e \"SELECT LEFT(DIGEST_TEXT, 20) AS statement, COUNT_STAR AS cnt, ROUND(AVG_TIMER_WAIT / 1e9, 3) AS avg_ms, SUM_ROWS_EXAMINED AS rows_examined FROM performance_schema.events_statements_summary_by_digest WHERE SCHEMA_NAME = 'car_rental' AND DIGEST_TEXT LIKE 'SELECT \\\`r\\\` . \\\`rental_id\\\`%'\""
  # повернення налаштувань
  $M -e "SET GLOBAL slow_query_log = OFF; SET GLOBAL long_query_time = 10; SET GLOBAL log_output = 'FILE'"
  showd "mysql.sh -N -e \"SHOW GLOBAL VARIABLES WHERE Variable_name IN ('slow_query_log', …)\"" \
    "$M -N -e \"SHOW GLOBAL VARIABLES WHERE Variable_name IN ('slow_query_log', 'long_query_time', 'log_output')\""
}

lock() {
  # сеанс A тримає блокування рядка, сеанс B чекає, сеанс C дивиться на очікування
  printf '$ %s\n' "mysql.sh car_rental -e \"START TRANSACTION; UPDATE cars …; DO SLEEP(10); ROLLBACK\" &   # сеанс A"
  $M car_rental -e "START TRANSACTION; UPDATE cars SET mileage_km = mileage_km WHERE car_id = 1; DO SLEEP(10); ROLLBACK" >/dev/null 2>&1 &
  sleep 2
  printf '$ %s\n' "mysql.sh car_rental -e \"UPDATE cars SET status = status WHERE car_id = 1\" &   # сеанс B"
  $M car_rental -e "UPDATE cars SET status = status WHERE car_id = 1" >/dev/null 2>&1 &
  sleep 2
  showd "mysql.sh -E -e \"SELECT locked_table, wait_age_secs, waiting_pid, waiting_query, … FROM sys.innodb_lock_waits\"" \
    "$M -E -e \"SELECT locked_table, wait_age_secs, waiting_pid, waiting_query, blocking_pid, blocking_query FROM sys.innodb_lock_waits\""
  showd "mysql.sh -t -e \"SELECT object_name, lock_type, lock_mode, … FROM performance_schema.data_locks WHERE …\"" \
    "$M -t -e \"SELECT object_name, lock_type, lock_mode, lock_status, lock_data FROM performance_schema.data_locks WHERE object_name = 'cars'\""
  wait
  echo "# Сеанс A завершився ROLLBACK, сеанс B виконався"
}

binlog() {
  # двійковий журнал: позиція до зміни, подія UPDATE у режимі ROW (mysqlbinlog в образі mysql:8.4 немає)
  show "$M -N -e \"SHOW BINARY LOG STATUS\""
  LOG=$($M -N -e "SHOW BINARY LOG STATUS" | cut -f1)
  POS=$($M -N -e "SHOW BINARY LOG STATUS" | cut -f2)
  show "$M car_rental -e \"UPDATE cars SET mileage_km = mileage_km + 100 WHERE car_id = 1\""
  show "$M -t -e \"SHOW BINLOG EVENTS IN '$LOG' FROM $POS\""
  show "$M -N -e \"SHOW VARIABLES WHERE Variable_name IN ('log_bin', 'binlog_format')\""
  $M car_rental -e "UPDATE cars SET mileage_km = mileage_km - 100 WHERE car_id = 1"
}

errlog() {
  # журнал помилок: невдалий вхід видно лише при log_error_verbosity = 3
  T0=$($M -N -e "SELECT NOW(6)")
  Q="SELECT COUNT(*) AS login_errors FROM performance_schema.error_log WHERE error_code = 'MY-010926' AND logged > '$T0'"
  show "$M -N -e \"SELECT @@log_error_verbosity\""
  show "MYSQL_PWD=wrong $M -e \"SELECT 1\""
  show "$M -t -e \"$Q\""
  show "$M -e \"SET GLOBAL log_error_verbosity = 3\""
  show "MYSQL_PWD=wrong $M -e \"SELECT 1\""
  show "$M -E -e \"SELECT logged, prio, error_code, LEFT(data, 62) AS data FROM performance_schema.error_log WHERE error_code = 'MY-010926' AND logged > '$T0'\""
  $M -e "SET GLOBAL log_error_verbosity = 2"
}

case "${1:-all}" in
  slow) slow ;;
  lock) lock ;;
  binlog) binlog ;;
  errlog) errlog ;;
  all) slow; lock; binlog; errlog ;;
  *) echo "Використання: $0 slow | lock | binlog | errlog | all"; exit 1 ;;
esac
