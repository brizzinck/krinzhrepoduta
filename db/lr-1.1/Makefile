# ЛР 1.1: додаткові запити на захисті ПР 1.1. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db t1 t2 t3 t4 t5 queries show
SH := .
D := car_rental_defense

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа копія car_rental_defense зі схеми й даних ПР 1.1
> ./mysql.sh < car_rental_defense.sql
> @echo "база car_rental_defense створена"

t1: db ## завдання 1: нова таблиця зі зв'язком (sql/01-create-table.sql)
> @echo; echo "Крок 1. Нова таблиця insurance_policies зі зв'язком до cars"
> ./mysql.sh -t -vvv $(D) < sql/01-create-table.sql | ./tidy.sh

t2: t1 ## завдання 2: ALTER TABLE і зміна даних (sql/02-alter-update.sql), після t1
> @echo; echo "Крок 2. Зміна структури cars і наявних даних"
> ./mysql.sh -t -vvv $(D) < sql/02-alter-update.sql | ./tidy.sh

t3: t2 ## завдання 3: масове підвищення ставки (sql/03-mass-update.sql), після t2
> @echo; echo "Крок 3. Масове підвищення ставки класу «Економ» на 10 %"
> ./mysql.sh -t -vvv $(D) < sql/03-mass-update.sql | ./tidy.sh

t4: t3 ## завдання 4: видалення з каскадом (sql/04-delete-cascade.sql), після t3
> @echo; echo "Крок 4. Видалення авто без оренд і каскад на поліси"
> ./mysql.sh -t -vvv $(D) < sql/04-delete-cascade.sql | ./tidy.sh

t5: t4 ## завдання 5: вибірка із з'єднанням (sql/05-join-select.sql), після t4
> @echo; echo "Крок 5. Вибірка із з'єднанням: хто зараз орендує авто"
> ./mysql.sh -t -vvv $(D) < sql/05-join-select.sql | ./tidy.sh

queries: t5 ## усі п'ять завдань зі свіжою копією
show: t5 ## усе по черзі
> @echo; echo "Готово."
