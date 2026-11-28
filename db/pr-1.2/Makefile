# ПР 1.2: запити вибірки, Northwind. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db q1 q2 q3 q4 q5 q6 q7 show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа база northwind
> ./mysql.sh < northwind.sql

q1: ## запит 1: продукти дорожчі за 20 (37 рядків) (sql/01-products-over-20.sql)
> @echo; echo "Крок 1. Запит 1: продукти дорожчі за 20 (37 рядків)"
> ./mysql.sh -t -vvv northwind < sql/01-products-over-20.sql | ./tidy.sh

q2: ## запит 2: замовлення у вівторок (168 рядків) (sql/02-orders-tuesday.sql)
> @echo; echo "Крок 2. Запит 2: замовлення у вівторок (168 рядків)"
> ./mysql.sh -t -vvv northwind < sql/02-orders-tuesday.sql | ./tidy.sh

q3: ## запит 3: середня ціна по категоріях (8 рядків) (sql/03-avg-price-by-category.sql)
> @echo; echo "Крок 3. Запит 3: середня ціна по категоріях (8 рядків)"
> ./mysql.sh -t -vvv northwind < sql/03-avg-price-by-category.sql | ./tidy.sh

q4: ## запит 4: співробітники й клієнти по містах (5 рядків) (sql/04-employees-customers-by-city.sql)
> @echo; echo "Крок 4. Запит 4: співробітники й клієнти по містах (5 рядків)"
> ./mysql.sh -t -vvv northwind < sql/04-employees-customers-by-city.sql | ./tidy.sh

q5: ## запит 5: дохід клієнтів (89 рядків) (sql/05-customer-revenue.sql)
> @echo; echo "Крок 5. Запит 5: дохід клієнтів (89 рядків)"
> ./mysql.sh -t -vvv northwind < sql/05-customer-revenue.sql | ./tidy.sh

q6: ## запит 6: замовлення зі співробітником, підзапит (830 рядків) (sql/06-orders-employee-subquery.sql)
> @echo; echo "Крок 6. Запит 6: замовлення зі співробітником, підзапит (830 рядків)"
> ./mysql.sh -t -vvv northwind < sql/06-orders-employee-subquery.sql | ./tidy.sh

q7: ## запит 7: UNION ALL постачальників і клієнтів Німеччини (14 рядків) (sql/07-union-all-germany.sql)
> @echo; echo "Крок 7. Запит 7: UNION ALL постачальників і клієнтів Німеччини (14 рядків)"
> ./mysql.sh -t -vvv northwind < sql/07-union-all-germany.sql | ./tidy.sh

show: db q1 q2 q3 q4 q5 q6 q7 ## усе по черзі
> @echo; echo "Готово."
