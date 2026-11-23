# ПР 2: модифікатори доступу, конструктори, this, static, final (Рахунок). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build access compile-ok run final show clean
T := ../../tools
CP := target/classes

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## mvn clean package (docker, JDK 21)
> @echo; echo "Крок 1. Збірка проєкту. Очікувано: BUILD SUCCESS"
> $(T)/mvn.sh clean package | grep -E 'Compiling|BUILD'

$(CP):
> @$(MAKE) --no-print-directory build

access: $(CP) ## доступ до полів: помилки javac для моделі й для Main (E1, E2)
> @echo; echo "Крок 2. Доступ до полів Account. Очікувано: E1 одна помилка (pin), E2 три помилки (pin, balance, number)"
> $(T)/javac.sh -encoding UTF-8 -cp $(CP) -d /tmp/out compile-errors/E1_access_model/*.java 2>&1 | grep -E 'error' || true
> $(T)/javac.sh -encoding UTF-8 -cp $(CP) -d /tmp/out compile-errors/E2_access_app/*.java 2>&1 | grep -E 'error' || true

compile-ok: $(CP) ## робочий код без хибних звернень компілюється
> @echo; echo "Крок 3. Робочий код компілюється. Очікувано: жодного повідомлення, код 0"
> $(T)/javac.sh -encoding UTF-8 -d /tmp/out $$(find src/main/java -name '*.java'); echo "код завершення: $$?"

run: $(CP) ## Main: доступ, конструктори, this, static, final
> @echo; echo "Крок 4. Запуск Main (п'ять дослідів). Очікувано: підрозділи Дослід 1 ... Дослід 5"
> $(T)/java.sh -cp $(CP) ua.duikt.lavrishko.pr2.app.Main

final: $(CP) ## спроби змінити final-поле і final-змінну (E3, E4)
> @echo; echo "Крок 5. Перезапис final. Очікувано: cannot assign a value to final variable id і limit"
> $(T)/javac.sh -encoding UTF-8 -cp $(CP) -d /tmp/out compile-errors/E3_final_field/*.java 2>&1 | grep -E 'error' || true
> $(T)/javac.sh -encoding UTF-8 -cp $(CP) -d /tmp/out compile-errors/E4_final_local/*.java 2>&1 | grep -E 'error' || true

show: build access compile-ok run final ## усе по черзі
> @echo; echo "Готово."

clean: ## видалити target/
> rm -rf target
