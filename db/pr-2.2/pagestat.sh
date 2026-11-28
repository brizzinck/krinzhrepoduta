#!/bin/sh
# Статистика сторінок test_index: те саме, що stat.sh, але друкує SQL і команди, які виконує.
# SQL лежить у sql/stat-*.sql. Використання: ./pagestat.sh   або   ./pagestat.sh -p   (лише сторінки)
set -eu
cd "$(dirname "$0")"
DB=northwind_opt
T=test_index

echo "Статистика сторінок $T"
echo "1. ./mysql.sh -N -B -vvv $DB < sql/stat-1-export.sql"
./mysql.sh -N -B -vvv "$DB" < sql/stat-1-export.sql
COPY=$(mktemp)
docker exec duikt-mysql cat "/tmp/$T.ibd" > "$COPY"
docker exec duikt-mysql rm -f "/tmp/$T.ibd"

echo "2. ./mysql.sh -N -B $DB < sql/stat-2-indexes.sql   (результат іде в ibdstat.py)"
sed '/^--/d' sql/stat-2-indexes.sql
IDX=$(./mysql.sh -N -B "$DB" < sql/stat-2-indexes.sql)
echo "3. python3 ibdstat.py <копія $T.ibd> $(echo $IDX)"
python3 ibdstat.py "$COPY" $IDX
rm -f "$COPY"

[ "${1:-}" = "-p" ] && exit 0
echo "4. ./mysql.sh -t -vvv $DB < sql/stat-3-usage.sql"
./mysql.sh -t -vvv "$DB" < sql/stat-3-usage.sql
