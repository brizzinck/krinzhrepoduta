# ЛР 2.1: SAVEPOINT, процедура, тригер на UPDATE, функція, view, роль. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db proc savepoint trigger func view role defense show
SH := .
M := $(SH)/mysql.sh -t -vvv northwind_defense

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа копія northwind_defense
> ./mysql.sh < northwind_defense.sql

proc: db ## процедура ShipProduct, SERIALIZABLE (sql/01); очікувано: Success, потім Failed
> @echo; echo "Крок 1. Процедура ShipProduct: списання залишку з перевіркою. Очікувано: Success, потім Failed"
> $(M) < sql/01-ship-product.sql | ./tidy.sh

savepoint: db ## SAVEPOINT: відкат частини транзакції (sql/02)
> @echo; echo "Крок 2. SAVEPOINT: відкат частини транзакції"
> $(M) < sql/02-savepoint.sql | ./tidy.sh

trigger: db ## тригер на UPDATE зі старою й новою ціною (sql/03)
> @echo; echo "Крок 3. Тригер на UPDATE зі старою й новою ціною"
> $(M) < sql/03-update-trigger.sql | ./tidy.sh

func: db ## функція OrderTotal у SELECT (sql/04)
> @echo; echo "Крок 4. Функція OrderTotal у SELECT"
> $(M) < sql/04-function.sql | ./tidy.sh

view: db ## оновлюваність view (sql/05); очікувано: ERROR 1288
> @echo; echo "Крок 5. Оновлюваність view. Очікувано: ERROR 1288 (view з GROUP BY не оновлюється)"
> $(M) --force < sql/05-view-updatable.sql | ./tidy.sh

role: db ## роль nw_reader і користувач lavrishko_ro (sql/06)
> @echo; echo "Крок 6. Роль замість окремих привілеїв"
> $(M) < sql/06-role.sql | ./tidy.sh

defense: proc savepoint trigger func view role ## усі шість прикладів по черзі

show: db defense ## усе по черзі
> @echo; echo "Готово."
