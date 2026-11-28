# СР 5.3: DBaaS і TLS-підключення застосунку до car_rental. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help up db build tls modes grants sql show
SH := .

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

up: ## запустити MySQL у docker
> @$(SH)/mysql-up.sh

db: up ## пересоздати car_rental (як у ПР 1.1)
> ./mysql.sh < schema.sql
> ./mysql.sh < seed.sql

build: ## зібрати jar застосунку ПР 3.1, якщо його немає
> @test -f pr-3.1/target/rentalapp.jar || (cd pr-3.1 && ./mvn.sh -q package)

tls: db build ## застосунок по TLS: шифр і версія з сервера (8 с)
> @echo; echo "Крок 1. Застосунок по TLS: шифр і версія з сервера (8 с) (SQL: sql/01-app-session-tls.sql)"
> @sh demo/tls-app.sh

modes: db build ## без TLS, вимога TLS на сервері, VERIFY_IDENTITY
> @echo; echo "Крок 2. Без TLS, вимога TLS на сервері, VERIFY_IDENTITY (SQL: sql/02-require-secure-transport.sql, sql/03-ssl-cipher-no-tls.sql). Очікувано: Communications link failure, бо застосунок не має CA, щоб перевірити сертифікат сервера"
> @sh demo/tls-modes.sh

grants: up ## права локального користувача rental (лише перегляд)
> @echo; echo "Окремо. Права локального користувача rental, лише перегляд (SQL: sql/04-show-grants.sql)"
> ./mysql.sh -t -vvv < sql/04-show-grants.sql

sql: ## показати весь SQL з теки sql/
> cat sql/*.sql

show: tls modes ## усе по черзі
> @echo; echo "Готово."
