#!/bin/sh
# Створює базу Northwind у контейнері duikt-mysql із дампа викладачки та виправляє дефекти дампа.
# Використання: setup_northwind.sh [імʼя_бази] [шлях_до_MY_SQL_Labwork2.sql] [шлях_до_LabWork2.sql]
# Приклад: setup_northwind.sh northwind_opt
# Самі дампи в репозиторій не входять: беруться з архіву Classroom (за замовчуванням) або з аргументів.
# Контейнер має працювати (../mysql-up.sh).
set -eu
HERE=$(cd "$(dirname "$0")" && pwd)
DB=${1:-northwind}
ROOT=$(cd "$HERE/../../../../../.." && pwd)
SUB="$ROOT/classroom/2026 Технології проектування та адміністрування баз даних та сховищ даних/1.ЛР+ПР Реляційні бази даних/13 - ПР № 1.2 Запити вибірки в реляційні бази даних"
MYSQL_DUMP=${2:-"$SUB/MY_SQL Labwork2.sql"}
TSQL_DUMP=${3:-"$SUB/LabWork2.sql"}
M="$HERE/../mysql.sh"

# 1. завантаження дампа з перейменуванням бази (LC_ALL=C: у дампі є двійкові дані)
echo "DROP DATABASE IF EXISTS \`$DB\`;" | "$M"
LC_ALL=C sed "s/\`northwind\`/\`$DB\`/g" "$MYSQL_DUMP" | "$M"

# 2. знижки: у дампі float(24,0) і всі значення дорівнюють 0; справжні беремо з T-SQL-дампа
python3 -I "$HERE/gen_discount_fix.py" "$TSQL_DUMP" > "$HERE/northwind_discount_fix.sql"
"$M" "$DB" < "$HERE/northwind_discount_fix.sql"

# 3. у дампі втрачено IDENTITY: products.ProductID без AUTO_INCREMENT
"$M" "$DB" -e "SET foreign_key_checks = 0; ALTER TABLE products MODIFY ProductID INT NOT NULL AUTO_INCREMENT; SET foreign_key_checks = 1;"
echo "Northwind створено в базі $DB"
