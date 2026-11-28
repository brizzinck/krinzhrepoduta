#!/bin/sh
# Демонстрація логування й моніторингу MySQL 8.4 на базі car_rental у контейнері duikt-mysql.
# Використання: ./demo.sh slow | lock | binlog | errlog | all
# Увесь SQL лежить у теці sql/ (по одному файлу на крок). Скрипт лише запускає ці файли й друкує команду;
# сам запит видно у виводі завдяки -vvv (клієнт mysql друкує текст запиту перед результатом).
# Змінюються лише змінні рівня сервера (SET GLOBAL), вони повертаються в кінці кожної частини.
# Дані car_rental залишаються без змін: тестові оновлення скасовуються або повертаються.
cd "$(dirname "$0")"
M=./mysql.sh

# run ОПЦІЇ ФАЙЛ: друкує команду як у терміналі й виконує файл SQL через mysql.sh
run() { printf '$ %s %s < %s\n' "$M" "$1" "$2"; $M $1 < "$2" 2>&1; }

# те, що працює без -vvv (запит іде від сеансу чи з хибним паролем): друкуємо файл, щоб SQL було видно.
# Коментарі в цих файлах з "--": клієнт їх прибирає, тому текст запиту на сервері без змін (потрібно для slow_log і innodb_lock_waits).
show() { printf '$ cat %s\n' "$1"; cat "$1"; }

slow() {
  # повільний журнал у таблицю mysql.slow_log, поріг 0: фіксується кожен запит
  run "-vvv" sql/01-slow-enable.sql
  show sql/02-slow-workload.sql
  printf '$ %s\n' "for i in 1 2 3; do ./mysql.sh car_rental < sql/02-slow-workload.sql; done"
  for i in 1 2 3; do $M car_rental < sql/02-slow-workload.sql >/dev/null; done
  run "-t -vvv" sql/03-slow-read-slow-log.sql
  run "-t -vvv" sql/04-slow-read-digest.sql
  # повернення налаштувань
  run "-vvv" sql/05-slow-restore.sql
  run "-vvv" sql/06-slow-check-variables.sql
}

lock() {
  # сеанс A тримає блокування рядка, сеанс B чекає, сеанс C дивиться на очікування
  show sql/07-lock-session-a.sql
  printf '$ %s\n' "./mysql.sh car_rental < sql/07-lock-session-a.sql &   # сеанс A: UPDATE, DO SLEEP(10), ROLLBACK"
  $M car_rental < sql/07-lock-session-a.sql >/dev/null 2>&1 &
  sleep 2
  show sql/08-lock-session-b.sql
  printf '$ %s\n' "./mysql.sh car_rental < sql/08-lock-session-b.sql &   # сеанс B: той самий рядок, чекає"
  $M car_rental < sql/08-lock-session-b.sql >/dev/null 2>&1 &
  sleep 2
  run "-E -vvv" sql/09-lock-waits.sql
  run "-t -vvv" sql/10-lock-data-locks.sql
  wait
  echo "# Сеанс A завершився ROLLBACK, сеанс B виконався"
}

binlog() {
  # двійковий журнал: позиція до зміни, подія UPDATE у режимі ROW (mysqlbinlog в образі mysql:8.4 немає)
  run "-vvv" sql/11-binlog-status.sql
  LOG=$($M -N < sql/11-binlog-status.sql | cut -f1)
  POS=$($M -N < sql/11-binlog-status.sql | cut -f2)
  run "-vvv car_rental" sql/12-binlog-update.sql
  # SHOW BINLOG EVENTS не приймає змінних: ім'я файла й позицію підставляє sed
  # cut -c17- прибирає стовпець Log_name (ім'я файла вже є в команді), щоб вивід вміщався в 92 колонки
  printf '$ %s\n' "sed -e \"s/@LOG@/$LOG/\" -e \"s/@POS@/$POS/\" sql/13-binlog-events.sql | ./mysql.sh -t | cut -c17-"
  printf '# SHOW BINLOG EVENTS IN '"'%s'"' FROM %s;\n' "$LOG" "$POS"
  sed -e "s/@LOG@/$LOG/" -e "s/@POS@/$POS/" sql/13-binlog-events.sql | $M -t 2>&1 | cut -c17-
  run "-vvv" sql/14-binlog-variables.sql
  $M car_rental < sql/15-binlog-revert.sql
}

errlog() {
  # журнал помилок: невдалий вхід видно лише при log_error_verbosity = 3
  T0=$($M -N < sql/16-errlog-start-time.sql)
  # підставляє момент початку замість @T0@ і виконує файл
  runt() { printf '$ %s\n' "sed \"s/@T0@/$T0/\" $2 | ./mysql.sh $1"; sed "s/@T0@/$T0/" "$2" | $M $1 2>&1; }
  run "-vvv" sql/17-errlog-verbosity.sql
  show sql/19-errlog-failed-login.sql
  printf '$ %s\n' "MYSQL_PWD=wrong ./mysql.sh < sql/19-errlog-failed-login.sql"
  MYSQL_PWD=wrong $M < sql/19-errlog-failed-login.sql 2>&1
  runt "-t -vvv" sql/18-errlog-count.sql
  run "-vvv" sql/20-errlog-verbosity-3.sql
  show sql/19-errlog-failed-login.sql
  printf '$ %s\n' "MYSQL_PWD=wrong ./mysql.sh < sql/19-errlog-failed-login.sql"
  MYSQL_PWD=wrong $M < sql/19-errlog-failed-login.sql 2>&1
  runt "-E -vvv" sql/21-errlog-entry.sql
  $M < sql/22-errlog-verbosity-2.sql
}

case "${1:-all}" in
  slow) slow ;;
  lock) lock ;;
  binlog) binlog ;;
  errlog) errlog ;;
  all) slow; lock; binlog; errlog ;;
  *) echo "Використання: $0 slow | lock | binlog | errlog | all"; exit 1 ;;
esac
