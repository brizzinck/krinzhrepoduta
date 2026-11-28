# СР 5.4: порівняння JDBC, Micro ORM і ORM (ПР 3.1, 3.2, 3.3). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db queries loc nplus1 sql showsql apps show
SHELL := /bin/bash
SH := .
P1 := pr-3.1/src/main/java/rental
P2 := pr-3.2/src/main/java/rental
P3 := pr-3.3/src/main/java/northwind

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## база northwind_orm на версії 2 (через ПР 3.3)
> @$(MAKE) -s -C pr-3.3 migrate > /dev/null

queries: ## порівнювані запити: JDBC, Jdbi, Hibernate (JPQL і SQL, який він генерує)
> @echo; echo "Крок 1. Порівнювані запити в трьох версіях (SQL: sql/01-jdbc.sql, sql/02-jdbi.sql, sql/03-hibernate-jpql-and-generated.sql)"
> cat sql/01-jdbc.sql sql/02-jdbi.sql sql/03-hibernate-jpql-and-generated.sql

loc: ## рядки ручного коду: JDBC, Jdbi, Hibernate і згенеровані сутності
> @echo; echo "Крок 2. Рядки ручного коду: JDBC, Jdbi, Hibernate і згенеровані сутності"
> @wc -l $(P1)/{Database,Table,TableDao,QueryDao}.java | tail -1
> @wc -l $(P2)/{Database,Branch,Customer,Car,ActiveRental,FreeCar,BranchRevenue,BranchDao,CustomerDao,CarDao,ReportDao}.java | tail -1
> @wc -l $(P3)/{Database,Queries}.java | tail -1
> @wc -l $(P3)/entity/*.java | tail -1

nplus1: db ## Hibernate: N+1 (6 запитів проти 1 з join fetch)
> @echo; echo "Крок 3. Hibernate: N+1 (6 запитів проти 1 з join fetch) (SQL: sql/04-n-plus-1.sql)"
> cat sql/04-n-plus-1.sql
> @cd pr-3.3 && ./run.sh < demo/03-n-plus-1.txt | sed -n '/Ваш вибір: 7/,/join fetch/p'

sql: db ## Hibernate: SQL, який генерує бібліотека (запит 6)
> @echo; echo "Крок 4. Hibernate: SQL, який генерує бібліотека (запит 6) (SQL: sql/03-hibernate-jpql-and-generated.sql)"
> @cd pr-3.3 && SHOW_SQL=true ./run.sh < demo/04-show-sql.txt | sed -n '/Ваш вибір: 6/,/^orderId/p'

showsql: ## показати весь SQL з теки sql/
> cat sql/*.sql

apps: ## усі три додатки по черзі (довго)
> @echo; echo "Окремо. Усі три додатки по черзі (довго)"
> @$(MAKE) -s -C pr-3.1 show
> @$(MAKE) -s -C pr-3.2 show
> @$(MAKE) -s -C pr-3.3 show

show: queries loc nplus1 sql ## усе по черзі, крім apps
> @echo; echo "Готово."
