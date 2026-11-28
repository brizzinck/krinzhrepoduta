# ЛР 1.2: SELECT, групування, JOIN, множинні операції, підзапити. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db c1 c2 c3 c4 show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа база northwind
> ./mysql.sh < northwind.sql

c1: ## DISTINCT, ORDER BY, NULL, діапазон, LIKE, IN, функції (sql/01-select-where.sql)
> @echo; echo "Крок 1. DISTINCT, ORDER BY, NULL, діапазон, LIKE, IN, функції"
> ./mysql.sh -t -vvv -n --force northwind < sql/01-select-where.sql | ./tidy.sh

c2: ## GROUP BY, JOIN, емуляція FULL JOIN (прямий FULL OUTER JOIN дає помилку) (sql/02-group-join.sql)
> @echo; echo "Крок 2. GROUP BY, JOIN, емуляція FULL JOIN (прямий FULL OUTER JOIN дає помилку)"
> ./mysql.sh -t -vvv -n --force northwind < sql/02-group-join.sql | ./tidy.sh

c3: ## UNION, INTERSECT, EXCEPT, MINUS (помилка) і підзапити (sql/03-sets-subq.sql)
> @echo; echo "Крок 3. UNION, INTERSECT, EXCEPT, MINUS (помилка) і підзапити"
> ./mysql.sh -t -vvv -n --force northwind < sql/03-sets-subq.sql | ./tidy.sh

c4: ## топ-5 співробітників за кількістю замовлень (sql/04-top-employees.sql)
> @echo; echo "Крок 4. Топ-5 співробітників за кількістю замовлень"
> ./mysql.sh -t -vvv -n --force northwind < sql/04-top-employees.sql | ./tidy.sh

show: db c1 c2 c3 c4 ## усе по черзі
> @echo; echo "Готово."
