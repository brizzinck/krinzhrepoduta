# ПР 1.1: база прокату автомобілів car_rental. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db counts sample ddl check show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## створити з нуля car_rental і її копію car_rental_check
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql
> ./mysql.sh < car_rental_check.sql

counts: ## кількість рядків у 10 таблицях (sql/01-counts.sql)
> @echo; echo "Крок 1. Кількість рядків у 10 таблицях"
> ./mysql.sh -t -vvv car_rental < sql/01-counts.sql | ./tidy.sh

sample: ## приклад даних: п'ять авто (sql/02-sample.sql)
> @echo; echo "Крок 2. Приклад даних: п'ять авто"
> ./mysql.sh -t -vvv car_rental < sql/02-sample.sql | ./tidy.sh

ddl: ## обмеження таблиці rentals, FK і CHECK (sql/03-ddl.sql)
> @echo; echo "Крок 3. Обмеження таблиці rentals (FK, CHECK)"
> ./mysql.sh -vvv --skip-comments car_rental < sql/03-ddl.sql | grep -E 'CONSTRAINT|SHOW CREATE'

check: db ## перевірка обмежень на копії, спершу пересоздає бази (sql/04-check.sql)
> @echo; echo "Крок 4. Перевірка обмежень на копії car_rental_check. Очікувано: ERROR 3819, 1062, 1452, 1451, тобто обмеження спрацьовують"
> ./mysql.sh --force -t -vvv car_rental_check < sql/04-check.sql | ./tidy.sh

show: db counts sample ddl check ## усе по черзі
> @echo; echo "Готово."
