# СР 5.1: реляційна алгебра і SQL на базі car_rental. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db union intersect except project join full divide diff show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## пересоздати car_rental (як у ПР 1.1)
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql

union: ## 1. об'єднання UNION і UNION ALL (sql/01-union.sql)
> @echo; echo "Крок 1. Об'єднання UNION і UNION ALL"
> ./mysql.sh -t -vvv --force car_rental < sql/01-union.sql | ./tidy.sh

intersect: ## 2. перетин INTERSECT (sql/02-intersect.sql)
> @echo; echo "Крок 2. Перетин INTERSECT"
> ./mysql.sh -t -vvv --force car_rental < sql/02-intersect.sql | ./tidy.sh

except: ## 3. різниця EXCEPT, MINUS дає помилку (sql/03-except.sql)
> @echo; echo "Крок 3. Різниця EXCEPT, MINUS дає помилку"
> ./mysql.sh -t -vvv --force car_rental < sql/03-except.sql | ./tidy.sh

project: ## 4. проєкція, вибірка, декартів добуток (sql/04-projection-selection-product.sql)
> @echo; echo "Крок 4. Проєкція, вибірка, декартів добуток"
> ./mysql.sh -t -vvv --force car_rental < sql/04-projection-selection-product.sql | ./tidy.sh

join: ## 5. θ-, екві-, природне, напів- і антиз'єднання (sql/05-joins.sql)
> @echo; echo "Крок 5. θ-, екві-, природне, напів- і антиз'єднання"
> ./mysql.sh -t -vvv --force car_rental < sql/05-joins.sql | ./tidy.sh

full: ## 6. FULL OUTER JOIN і його емуляція (sql/06-full-join.sql)
> @echo; echo "Крок 6. FULL OUTER JOIN і його емуляція. Очікувано: ERROR 1064, бо MySQL не має FULL OUTER JOIN"
> ./mysql.sh -t -vvv --force car_rental < sql/06-full-join.sql | ./tidy.sh

divide: ## 7. ділення через подвійне NOT EXISTS (sql/07-division.sql)
> @echo; echo "Крок 7. Ділення через подвійне NOT EXISTS"
> ./mysql.sh -t -vvv --force car_rental < sql/07-division.sql | ./tidy.sh

diff: ## 8. відмінності SQL від алгебри (sql/08-sql-vs-algebra.sql)
> @echo; echo "Крок 8. Відмінності SQL від алгебри"
> ./mysql.sh -t -vvv --force car_rental < sql/08-sql-vs-algebra.sql | ./tidy.sh

show: db union intersect except project join full divide diff ## усе по черзі
> @echo; echo "Готово."
