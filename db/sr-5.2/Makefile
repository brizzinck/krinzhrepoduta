# СР 5.2: логування й моніторинг MySQL на базі car_rental. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db slow lock binlog errlog sql show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## пересоздати car_rental (як у ПР 1.1)
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql

slow: ## повільний журнал: поріг 0, таблиця slow_log і digest
> @echo; echo "Крок 1. Повільний журнал: поріг 0, таблиця slow_log і digest (SQL: sql/01-slow-enable.sql ... sql/06-slow-check-variables.sql)"
> ./demo.sh slow

lock: ## блокування: два сеанси, sys.innodb_lock_waits (10 с)
> @echo; echo "Крок 2. Блокування: два сеанси, sys.innodb_lock_waits (10 с) (SQL: sql/07-lock-session-a.sql ... sql/10-lock-data-locks.sql)"
> ./demo.sh lock

binlog: ## двійковий журнал: подія Update_rows
> @echo; echo "Крок 3. Двійковий журнал: подія Update_rows (SQL: sql/11-binlog-status.sql ... sql/15-binlog-revert.sql)"
> ./demo.sh binlog

errlog: ## журнал помилок: невдалий вхід при verbosity 3
> @echo; echo "Крок 4. Журнал помилок: невдалий вхід при verbosity 3 (SQL: sql/16-errlog-start-time.sql ... sql/22-errlog-verbosity-2.sql). Очікувано: ERROR 1045 від навмисно хибного пароля"
> ./demo.sh errlog

sql: ## показати весь SQL з теки sql/
> cat sql/*.sql

show: db slow lock binlog errlog ## усе по черзі
> @echo; echo "Готово."
