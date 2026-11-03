#!/bin/sh
# Статистика таблиці: сторінки й рівні індексів (ibdstat.py) та обсяги (аналог sp_spaceused).
# Використання: ./stat.sh test_index   або   ./stat.sh test_index -p   (лише сторінки)
set -eu
cd "$(dirname "$0")"
T=$1
DB=northwind_opt
M=../_shared/mysql.sh

# Скидаємо сторінки на диск і копіюємо файл, поки таблицю заблоковано. Команда \! виконується в контейнері.
$M -N -B "$DB" <<SQL
FLUSH TABLES $T FOR EXPORT;
\! cp /var/lib/mysql/$DB/$T.ibd /tmp/$T.ibd
UNLOCK TABLES;
SQL
COPY=$(mktemp)
docker exec duikt-mysql cat "/tmp/$T.ibd" > "$COPY"
docker exec duikt-mysql rm -f "/tmp/$T.ibd"

# Назва індексу, його номер і коренева сторінка для ibdstat.py
IDX=$($M -N -B -e "SELECT CONCAT(i.NAME, '=', i.INDEX_ID, '@', i.PAGE_NO)
  FROM information_schema.INNODB_INDEXES i JOIN information_schema.INNODB_TABLES t USING (TABLE_ID)
  WHERE t.NAME = '$DB/$T' ORDER BY i.INDEX_ID")
python3 ibdstat.py "$COPY" $IDX
rm -f "$COPY"

[ "${2:-}" = "-p" ] && exit 0
$M "$DB" -e "ANALYZE TABLE $T" > /dev/null
$M -t "$DB" -e "SELECT TABLE_NAME AS name, TABLE_ROWS AS \`rows\`, (SELECT COUNT(*) FROM $T) AS exact_rows,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024) AS reserved_kb, ROUND(DATA_LENGTH / 1024) AS data_kb,
    ROUND(INDEX_LENGTH / 1024) AS index_kb, ROUND(DATA_FREE / 1024) AS unused_kb
  FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB' AND TABLE_NAME = '$T'"
