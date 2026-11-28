# ПР 2.1: транзакції, процедура, тригер, view, користувач. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db tx dirty committed proc trigger view user show
SH := .
M := $(SH)/mysql.sh -t -vvv
U := LavrishkoVladyslavTCR32
UM := MYSQL_PWD=Lavr_dev_2026 $(SH)/mysql.sh -u$(U) -t -vvv

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа база northwind
> ./mysql.sh < northwind.sql

tx: db ## завдання 1: UPDATE у транзакції і ROLLBACK (sql/01)
> @echo; echo "Крок 1. Завдання 1: UPDATE у транзакції і ROLLBACK"
> $(M) northwind < sql/01-transaction.sql | ./tidy.sh

dirty: db ## дві сесії, READ UNCOMMITTED: брудне читання (sql/02a, 02b; близько 8 с)
> @echo; echo "Крок 2. Дві сесії, READ UNCOMMITTED: брудне читання (близько 8 с)"
> $(M) -n northwind < sql/02a-dirty-session-A.sql & $(M) -n northwind < sql/02b-dirty-session-B.sql; wait

committed: db ## дві сесії, READ COMMITTED: незафіксоване не видно (sql/03a, 03b; близько 8 с)
> @echo; echo "Крок 3. Дві сесії, READ COMMITTED: незафіксоване не видно (близько 8 с)"
> $(M) -n northwind < sql/03a-committed-session-A.sql & $(M) -n northwind < sql/03b-committed-session-B.sql; wait

proc: db ## завдання 2: процедура AddProduct (sql/04)
> @echo; echo "Крок 4. Завдання 2: процедура AddProduct"
> $(M) northwind < sql/04-procedure.sql | ./tidy.sh

trigger: db ## завдання 3: журнал productslog і тригер (sql/05)
> @echo; echo "Крок 5. Завдання 3: журнал productslog і тригер"
> $(M) northwind < sql/05-log-trigger.sql | ./tidy.sh

view: db ## завдання 4: view із запиту 6 (sql/06)
> @echo; echo "Крок 6. Завдання 4: view із запиту 6"
> $(M) northwind < sql/06-view.sql | ./tidy.sh

user: db ## завдання 5: користувач, REVOKE, DROP USER (sql/07a-07g)
> @echo; echo "Крок 7. Завдання 5: користувач, REVOKE, DROP USER. Очікувано: ERROR 1142 після REVOKE і ERROR 1045 після DROP USER"
> $(M) < sql/07a-drop-old-user.sql | ./tidy.sh
> $(M) < sql/07b-create-user.sql | ./tidy.sh
> $(UM) northwind < sql/07c-user-ok.sql | ./tidy.sh
> $(M) < sql/07d-revoke.sql | ./tidy.sh
> $(UM) --force northwind < sql/07e-user-denied.sql | ./tidy.sh
> $(M) < sql/07f-drop-user.sql | ./tidy.sh
> $(UM) northwind < sql/07g-login-denied.sql | ./tidy.sh

show: db tx dirty committed proc trigger view user ## усе по черзі
> @echo; echo "Готово."
