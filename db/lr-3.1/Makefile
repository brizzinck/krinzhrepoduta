# ЛР 3.1: з'єднання JDBC, вставка, читання, видалення. Показ: make show
.RECIPEPREFIX := >
.PHONY: help db sql show

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

db: ## свіжа база car_rental
> @./mysql-up.sh >/dev/null
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql

sql: ## показати весь SQL з теки sql/
> cat sql/*.sql

show: db ## запустити ConnectDemo.java: з'єднання, вставка, читання, видалення
> @echo; echo "Крок 1. ConnectDemo.java: з'єднання, вставка, читання, видалення (SQL: sql/01-insert-extra.sql, sql/02-select-extras.sql, sql/03-delete-extra.sql)"
> cat sql/01-insert-extra.sql sql/02-select-extras.sql sql/03-delete-extra.sql
> @./run.sh
> @echo; echo "Готово."
