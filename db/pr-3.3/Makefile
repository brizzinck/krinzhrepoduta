# ПР 3.3 і ЛР 3.3: Hibernate, Flyway, N+1. Усе з нуля однією командою: make show
.RECIPEPREFIX := >
.PHONY: help up db build migrate queries nplus1 tables run validate sql show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up:
> @$(SH)/mysql-up.sh

db: up ## нова база northwind_orm версії 1 (13 таблиць, без колонки Email)
> ./mysql.sh < northwind_orm.sql

build: ## зібрати jar, якщо його немає
> @test -f target/northwindorm.jar || ./mvn.sh -q package

migrate: db build ## міграції Flyway: V1 базова, V2 додає Email
> @echo; echo "Крок 1. Міграції Flyway (SQL: sql/01-flyway-v1-schema.sql, sql/02-flyway-v2-add-customer-email.sql)"
> cat sql/01-flyway-v1-schema.sql sql/02-flyway-v2-add-customer-email.sql
> @./flyway.sh baseline | tail -1
> @./flyway.sh migrate | tail -1
> @./flyway.sh info | tail -6

queries: build ## запити 3-6 (JPQL)
> @echo; echo "Крок 2. Запити 3-6 (SQL: sql/03-query3-avg-price.sql, sql/04-query4-employees-customers-by-city.sql, sql/05-query5-revenue-by-customer.sql, sql/06-query6-orders-with-employee.sql)"
> cat sql/03-query3-avg-price.sql sql/04-query4-employees-customers-by-city.sql sql/05-query5-revenue-by-customer.sql sql/06-query6-orders-with-employee.sql
> @./run.sh < demo/02-queries-a.txt | sed -n '/Ваш вибір: 3/,$$p' | grep -v -e 'Ваш вибір: 0' -e 'Роботу завершено'
> @./run.sh < demo/02-queries-b.txt | sed -n '/Ваш вибір: 5/,$$p' | grep -v -e 'Ваш вибір: 0' -e 'Роботу завершено'

nplus1: build ## пункт 7: N+1 і join fetch
> @echo; echo "Крок 3. Ліниве завантаження: N+1 проти join fetch (SQL: sql/07-n-plus-1.sql)"
> cat sql/07-n-plus-1.sql
> @./run.sh < demo/03-n-plus-1.txt | sed -n '/Ваш вибір: 7/,$$p' | grep -v -e 'Ваш вибір: 0' -e 'Роботу завершено'

tables: build ## дані з усіх 13 таблиць
> @echo; echo "Крок 4. Дані з усіх 13 таблиць (SQL: sql/08-tables-1-13.sql)"
> cat sql/08-tables-1-13.sql
> @./run.sh < demo/05-all-tables.txt | sed -n '/Ваш вибір: 1/,$$p' | grep -v -e 'Ваш вибір: 0' -e 'Роботу завершено'

run: build ## меню для ручного вводу
> @./run.sh

validate: db build ## окремо: без V2 Hibernate не стартує (validate бачить, що колонки Email немає; SQL: sql/02-flyway-v2-add-customer-email.sql)
> @printf '0\n' | ./run.sh | sed -n 1p

sql: ## показати весь SQL з теки sql/
> cat sql/*.sql

show: migrate queries nplus1 tables ## усе з нуля по черзі
> @echo; echo "Готово: база на версії 2, запити 3-6, N+1 і всі таблиці показано"
