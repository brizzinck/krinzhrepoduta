# ПР 3.2 і ЛР 3.2: той самий додаток на Jdbi. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db build run queries crud errors sql show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа база car_rental (наступний ключ авто 25)
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql

build: ## зібрати jar (target/rentaljdbi.jar)
> @test -f target/rentaljdbi.jar || ./mvn.sh -q package

run: build ## меню для ручного вводу
> @./run.sh

queries: build ## запити 6, 7, 8: JOIN, фільтр з параметрами, агрегація
> @echo; echo "Крок 1. Запити 6, 7, 8: JOIN, фільтр з параметрами, агрегація (SQL: sql/01-query6-active-rentals.sql, sql/02-query7-free-cars.sql, sql/03-query8-branch-revenue.sql)"
> cat sql/01-query6-active-rentals.sql sql/02-query7-free-cars.sql sql/03-query8-branch-revenue.sql
> @./run.sh < demo/06-queries.txt

crud: db build ## додати, змінити, видалити авто з ключем 25
> @echo; echo "Крок 2. Додати, змінити, видалити авто з ключем 25 (SQL: sql/04-crud-insert-car.sql, sql/05-crud-update-car.sql, sql/06-crud-delete-car.sql)"
> cat sql/04-crud-insert-car.sql
> @./run.sh < demo/02-create.txt
> cat sql/05-crud-update-car.sql
> @./run.sh < demo/03-update.txt
> cat sql/06-crud-delete-car.sql
> @./run.sh < demo/04-delete.txt

errors: build ## помилки бази: 1062, 3819, 1452, 1451
> @echo; echo "Крок 3. Помилки бази 1062, 3819, 1452, 1451 і помилка вводу (SQL: sql/07-errors.sql). Очікувано: програма показує помилку й не падає"
> cat sql/07-errors.sql
> @./run.sh < demo/05-errors.txt

sql: ## показати весь SQL з теки sql/
> cat sql/*.sql

show: db queries crud errors ## усе по черзі
> @echo; echo "Готово."
