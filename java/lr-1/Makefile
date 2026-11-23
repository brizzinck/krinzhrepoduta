# ЛР 1: конвертер площі (варіант 6), fat-jar. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build table session errors multiword fatjar standalone incidents nocatch show clean
T := ../../tools
IMG := maven:3.9-eclipse-temurin-21
JAR := target/converter.jar
UID_GID = $$(id -u):$$(id -g)
RUN = docker run --rm -i -u "$(UID_GID)" -e LANG=C.UTF-8 -v "$$PWD":/w -w /w --entrypoint java $(IMG)

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## mvn clean package: тести JUnit і fat-jar (docker)
> @echo; echo "Крок 1. Збірка й тести. Очікувано: Tests run: 27, Failures: 0 і BUILD SUCCESS"
> $(T)/mvn.sh clean package | grep -E 'Tests run:.*Fail.*Skipped: 0$$|Building jar|BUILD'

$(JAR):
> @$(MAKE) --no-print-directory build

table: $(JAR) ## таблиця коефіцієнтів між усіма п'ятьма одиницями
> @echo; echo "Крок 2. Усі напрямки конвертації (--table). Очікувано: 5 рядків, на діагоналі 1"
> $(T)/java.sh -jar $(JAR) --table

session: $(JAR) ## сеанс меню: вибір одиниць за назвою й номером
> @echo; echo "Крок 3. Сеанс меню. Очікувано: 100 cm² = 0.01 m², 1 ha = 2.47105381467 acre"
> printf '7\n1\n100\ncm2\nm2\n1\nha\nacre\n2.5\nacre\nм2\n3\n0\n' | $(RUN) -jar $(JAR)

errors: $(JAR) ## некоректне введення без винятків
> @echo; echo "Крок 4. Хибне введення. Очікувано: повідомлення без стектрейсу, 1e308 acre у cm² дає завеликий результат"
> printf '1\nabc\nNaN\n1e999\n-5\n12,5\n1e308\n5\n1\n0\n' | $(RUN) -jar $(JAR)

multiword: $(JAR) ## два слова в одному рядку дають одне повідомлення
> @echo; echo "Крок 5. Два слова в рядку (7 8, -5 -6, foo bar). Очікувано: кожна помилка один раз, далі 5 m² = 0.0005 ha"
> printf '7 8\n1\n-5 -6\n5\nfoo bar\nm2\nha\n0\n' | $(RUN) -jar $(JAR)

fatjar: $(JAR) ## fat-jar: Main-Class і класи commons-lang3 усередині
> @echo; echo "Крок 6. Fat-jar. Очікувано: Main-Class, 413 записів commons-lang3 у converter.jar і 0 в original-converter.jar"
> @docker run --rm -u "$(UID_GID)" -v "$$PWD":/w -w /w --entrypoint sh $(IMG) -c \
>   'mkdir /tmp/m && cd /tmp/m && jar xf /w/$(JAR) META-INF/MANIFEST.MF && grep Main-Class META-INF/MANIFEST.MF | tr -d "\r"; \
>    echo "converter.jar: $$(jar tf /w/$(JAR) | grep -c org/apache/commons/lang3/)"; \
>    echo "original-converter.jar: $$(jar tf /w/target/original-converter.jar | grep -c org/apache/commons/lang3/)"'

standalone: $(JAR) ## запуск в образі лише з JRE, без Maven
> @echo; echo "Крок 7. Лише JRE і один файл. Очікувано: останній рядок таблиці (acre ... 1), хоча в образі лише JRE"
> docker run --rm -v "$$PWD/$(JAR):/app/converter.jar:ro" eclipse-temurin:21-jre java -jar /app/converter.jar --table | tail -1

incidents: $(JAR) ## звичайний JAR без залежності й маніфеста
> @echo; echo "Крок 8. original-converter.jar. Очікувано: no main manifest attribute і NoClassDefFoundError"
> $(T)/java.sh -jar target/original-converter.jar 2>&1 | head -1 || true
> $(T)/java.sh -cp target/original-converter.jar ua.duikt.lavrishko.converter.Main --table 2>&1 | grep NoClassDefFoundError || true

nocatch: ## у коді немає try і catch
> @echo; echo "Крок 9. Пошук try і catch у src/main. Очікувано: у кожному файлі 0"
> grep -rc -E 'catch|try' src/main/java || true

show: build table session errors multiword fatjar standalone incidents nocatch ## усе по черзі
> @echo; echo "Готово."

clean: ## видалити target/
> rm -rf target
