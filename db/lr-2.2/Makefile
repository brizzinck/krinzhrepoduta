# ЛР 2.2: функції над стовпцем, LIKE, невидимі індекси, EXISTS. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db func like selectivity invisible exists defense show
SH := .
M := $(SH)/mysql.sh -t -vvv northwind_defense

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## свіжа копія northwind_defense
> ./mysql.sh < northwind_defense.sql

func: db ## YEAR() над стовпцем проти діапазону дат (sql/01): До, Зміна, Після
> @echo; echo "Крок 1. YEAR() над стовпцем проти діапазону дат. Дивись стовпці type, key, rows"
> $(M) < sql/01-function-over-column.sql | ./tidy.sh

like: db ## LIKE '%a' проти 'A%' (sql/02): До, Зміна, Після
> @echo; echo "Крок 2. LIKE '%a' проти 'A%'. Дивись стовпці type, key, rows"
> $(M) < sql/02-like.sql | ./tidy.sh

selectivity: db ## Discontinued = 0 проти 1 (sql/03): До, Зміна, Після
> @echo; echo "Крок 3. Селективність: Discontinued = 0 проти 1. Дивись стовпці type, key, rows"
> $(M) < sql/03-selectivity.sql | ./tidy.sh

invisible: func ## INVISIBLE і VISIBLE індекс (sql/04): До, Зміна, Після
> @echo; echo "Крок 4. Невидимий індекс: ALTER INDEX INVISIBLE, потім VISIBLE. Дивись стовпці type, key, rows"
> $(M) < sql/04-invisible-index.sql | ./tidy.sh

exists: db ## DISTINCT після JOIN проти EXISTS (sql/05): До, Зміна, Після
> @echo; echo "Крок 5. DISTINCT після JOIN проти EXISTS. Дивись стовпець Extra: FirstMatch(c)"
> $(M) < sql/05-distinct-exists.sql | ./tidy.sh

defense: func like selectivity invisible exists ## усі п'ять прикладів по черзі

show: db defense ## усе по черзі
> @echo; echo "Готово."
