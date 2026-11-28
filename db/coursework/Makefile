# Курсова «Прокат мікромобільності»: усе працює в Docker через ці цілі.
# Запуск: make <ціль> (або make -C <ця тека> <ціль>); make help показує список.

DIR      := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
APP_PORT ?= 18480
URL      := http://127.0.0.1:$(APP_PORT)/
COMPOSE  := docker compose -f $(DIR)compose.yaml
export APP_PORT

.DEFAULT_GOAL := help
.PHONY: help up down reset logs ps db check test diagrams open clean show show-stack show-tables show-checks show-rules show-reports show-admin

help: ## Показати список цілей
	@grep -hE '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk -F ':.*## ' '{printf "  make %-13s %s\n", $$1, $$2}'

up: ## Зібрати й запустити MySQL і застосунок, дочекатися адмінки, надрукувати адресу
	$(COMPOSE) up -d --build
	@i=0; until [ "$$(curl -s -o /dev/null -w '%{http_code}' $(URL))" = 200 ]; do \
	  i=$$((i + 1)); [ $$i -le 90 ] || { echo "помилка: застосунок не відповів на $(URL)" >&2; exit 1; }; sleep 2; done
	@echo "Admin: $(URL)"

down: ## Зупинити стек, дані лишаються
	$(COMPOSE) down

reset: ## Скинути дані й запустити знову: свіжі демо-дані (активні поїздки з початкових даних стартують зараз)
	$(COMPOSE) down -v
	$(MAKE) --no-print-directory -f $(DIR)Makefile up

logs: ## Стежити за журналом застосунку
	$(COMPOSE) logs -f --tail=100 app

ps: ## Показати контейнери
	$(COMPOSE) ps

db: ## Консоль MySQL у контейнері db (облікові дані з середовища compose)
	$(COMPOSE) exec db sh -c 'MYSQL_PWD="$$MYSQL_PASSWORD" exec mysql -u"$$MYSQL_USER" --default-character-set=utf8mb4 "$$MYSQL_DATABASE"'

check: ## Усі перевірки на свіжому томі (./check.sh, код виходу 0, якщо все пройшло)
	$(DIR)check.sh

test: ## Юніт-тести (формула оплати) через Maven у Docker
	cd $(DIR)app && ../../../tools/mvn.sh -q test

diagrams: ## Перемалювати три моделі в diagrams/img зі схеми V1 у тимчасовому контейнері MySQL
	$(DIR)diagrams/make_diagrams.sh --throwaway

open: ## Відкрити адмінку в браузері
	xdg-open $(URL)

clean: ## Зупинити стек, видалити дані й app/target
	$(COMPOSE) down -v
	rm -rf $(DIR)app/target

show: ## Повний показ: reset (дані з нуля), потім усі кроки по черзі (show.sh)
	@echo "Скидання стеку: docker compose down -v, потім up -d --build (журнал збирання не показую)."
	@log=$$(mktemp); $(MAKE) --no-print-directory reset >$$log 2>&1 || { cat $$log; rm -f $$log; exit 1; }; rm -f $$log
	@echo "Стек запущено: $(URL)"
	@$(DIR)show.sh all

show-stack: ## Крок 1: контейнери й міграції Flyway
	@$(DIR)show.sh stack

show-tables: ## Крок 2: сім таблиць, кількість рядків, обмеження
	@$(DIR)show.sh tables

show-checks: ## Крок 3: помилки обмежень БД на окремій базі (sql/checks.sql)
	@$(DIR)show.sh checks

show-rules: ## Кроки 4-6: правила бізнесу через застосунок (потрібен свіжий стек)
	@$(DIR)show.sh rules

show-reports: ## Крок 7: звіти R1-R7 (sql/queries.sql)
	@$(DIR)show.sh reports

show-admin: ## Крок 8: сторінки адмінки й адреса для браузера
	@$(DIR)show.sh admin
