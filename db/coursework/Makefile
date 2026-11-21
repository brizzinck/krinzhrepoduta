# Micromobility rental coursework: everything runs in Docker through these targets.
# Usage: make <target>   (or: make -C <this directory> <target>); `make help` lists them.

DIR      := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
APP_PORT ?= 18480
URL      := http://127.0.0.1:$(APP_PORT)/
COMPOSE  := docker compose -f $(DIR)compose.yaml
export APP_PORT

.DEFAULT_GOAL := help
.PHONY: help up down reset logs ps db check test diagrams open clean

help: ## List the targets
	@grep -hE '^[a-z]+:.*## ' $(MAKEFILE_LIST) | awk -F ':.*## ' '{printf "  make %-9s %s\n", $$1, $$2}'

up: ## Build and start MySQL and the app, wait until the admin answers, print the URL
	$(COMPOSE) up -d --build
	@i=0; until [ "$$(curl -s -o /dev/null -w '%{http_code}' $(URL))" = 200 ]; do \
	  i=$$((i + 1)); [ $$i -le 90 ] || { echo "error: the app did not answer at $(URL)" >&2; exit 1; }; sleep 2; done
	@echo "Admin: $(URL)"

down: ## Stop the stack, keep the data
	$(COMPOSE) down

reset: ## Drop the data and start again: fresh demo data (active seed rides start now)
	$(COMPOSE) down -v
	$(MAKE) --no-print-directory -f $(DIR)Makefile up

logs: ## Follow the application log
	$(COMPOSE) logs -f --tail=100 app

ps: ## Show the containers
	$(COMPOSE) ps

db: ## MySQL shell in the db container (credentials from the compose environment)
	$(COMPOSE) exec db sh -c 'MYSQL_PWD="$$MYSQL_PASSWORD" exec mysql -u"$$MYSQL_USER" --default-character-set=utf8mb4 "$$MYSQL_DATABASE"'

check: ## Run all checks on a fresh volume (./check.sh, exit code 0 if everything passed)
	$(DIR)check.sh

test: ## Unit tests (billing formula) with Maven in Docker
	cd $(DIR)app && ../../../tools/mvn.sh -q test

diagrams: ## Redraw the three models into diagrams/img from V1 in a throwaway MySQL container
	$(DIR)diagrams/make_diagrams.sh --throwaway

open: ## Open the admin in the browser
	xdg-open $(URL)

clean: ## Stop the stack, drop the data, remove app/target
	$(COMPOSE) down -v
	rm -rf $(DIR)app/target
