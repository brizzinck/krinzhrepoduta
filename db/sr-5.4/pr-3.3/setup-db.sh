#!/bin/sh
# Створює базу northwind_orm з готового файла northwind_orm.sql (Northwind викладачки з виправленнями і правами для rental).
cd "$(dirname "$0")"
./mysql.sh < northwind_orm.sql
echo "Northwind створено в базі northwind_orm"
