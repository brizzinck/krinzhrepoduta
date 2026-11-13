#!/bin/sh
# Створює окрему базу northwind_orm (Northwind викладачки з виправленнями) і дає користувачу rental права на неї.
# Базу можна перестворити в будь-який момент: міграції цієї роботи змінюють лише northwind_orm.
set -eu
cd "$(dirname "$0")"
../_shared/northwind/setup_northwind.sh northwind_orm
../_shared/mysql.sh -e "GRANT ALL PRIVILEGES ON northwind_orm.* TO 'rental'@'%'"
