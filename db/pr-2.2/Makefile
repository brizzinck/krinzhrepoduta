# ПР 2.2: індекси, сторінки InnoDB, плани запитів. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db empty one rows30 newpage rows240 pk depth3 seq random optimize plan7 plan6 equiv show
SH := .
M := $(SH)/mysql.sh -t -vvv northwind_opt
ST := ./pagestat.sh
STP := ./pagestat.sh -p

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа база northwind_opt і службові процедури (sql/00)
> ./mysql.sh < northwind_opt.sql
> ./mysql.sh northwind_opt < sql/00-setup-procedures.sql > /dev/null && echo ok

empty: db ## sql/01: порожня таблиця без ключа, GEN_CLUST_INDEX
> @echo; echo "Крок 1. Методичка, крок 01: порожня таблиця без ключа, GEN_CLUST_INDEX"
> $(M) < sql/01-empty-table.sql | ./tidy.sh
> @$(ST)

one: empty ## sql/02: один рядок
> @echo; echo "Крок 2. Методичка, крок 02: один рядок"
> $(M) < sql/02-one-row.sql | ./tidy.sh
> @$(ST)

rows30: one ## sql/03: 30 рядків
> @echo; echo "Крок 3. Методичка, крок 03: 30 рядків"
> $(M) < sql/03-rows30.sql | ./tidy.sh
> @$(STP)

newpage: rows30 ## sql/04: рядок, що виділяє другу сторінку
> @echo; echo "Крок 4. Методичка, крок 04: рядок, що виділяє другу сторінку"
> $(M) < sql/04-new-page.sql | ./tidy.sh
> @$(STP)

rows240: newpage ## sql/05: 240 рядків
> @echo; echo "Крок 5. Методичка, крок 05: 240 рядків"
> $(M) < sql/05-rows240.sql | ./tidy.sh
> @$(STP)

pk: rows240 ## sql/06: PRIMARY KEY, 18630 послідовних рядків
> @echo; echo "Крок 6. Методичка, крок 07-08: PRIMARY KEY, 18630 послідовних рядків"
> $(M) < sql/06-primary-key.sql | ./tidy.sh
> @$(STP)

depth3: pk ## sql/07: 66138 рядків, третій рівень дерева
> @echo; echo "Крок 7. Методичка, крок 10b: 66138 рядків, третій рівень дерева"
> $(M) < sql/07-depth3.sql | ./tidy.sh
> @$(STP)

seq: depth3 ## sql/08: послідовний ключ, 9000 рядків
> @echo; echo "Крок 8. Методичка, крок 12: послідовний ключ, 9000 рядків"
> $(M) < sql/08-sequential-key.sql | ./tidy.sh
> @$(STP)

random: seq ## sql/09: випадковий ключ, фрагментація
> @echo; echo "Крок 9. Методичка, крок 14: випадковий ключ, фрагментація"
> $(M) < sql/09-random-key.sql | ./tidy.sh
> @$(STP)

optimize: random ## sql/10: OPTIMIZE TABLE прибирає фрагментацію
> @echo; echo "Крок 10. Методичка, крок 15: OPTIMIZE TABLE прибирає фрагментацію"
> $(M) < sql/10-optimize.sql | ./tidy.sh
> @$(STP)

plan7: db ## запит 7: EXPLAIN до і після CREATE INDEX (sql/11)
> @echo; echo "Крок 11. Запит 7: EXPLAIN до і після індексу (Country, CompanyName)"
> $(M) < sql/11-plan7-index.sql | ./tidy.sh

plan6: db ## запит 6: EXPLAIN ANALYZE, підзапит і LEFT JOIN (sql/12)
> @echo; echo "Крок 12. Запит 6: EXPLAIN ANALYZE, підзапит і LEFT JOIN. Дивись loops=830 і loops=1"
> $(SH)/mysql.sh -N -B -r -vvv northwind_opt < sql/12-plan6-rewrite.sql

equiv: db ## переписані запити дають той самий результат (sql/13)
> @echo; echo "Крок 13. Переписані запити дають той самий результат"
> $(M) < sql/13-equivalence.sql | ./tidy.sh

show: db empty one rows30 newpage rows240 pk depth3 seq random optimize plan7 plan6 equiv ## усе по черзі
> @echo; echo "Готово."
