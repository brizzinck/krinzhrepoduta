# ПР 1: Maven, JDK 21, залежність commons-lang3, javap. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build version run manifest nolib encoding javap show clean
T := ../../tools
IMG := maven:3.9-eclipse-temurin-21
JAR := target/env-check-1.0.jar
UID_GID = $$(id -u):$$(id -g)

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

version: ## версії JDK (java і javac)
> @echo; echo "Крок 1. Версії JDK. Очікувано: 21.0.x в обох командах"
> $(T)/java.sh -version 2>&1 | head -1
> $(T)/javac.sh -version

build: ## mvn clean package (docker, Maven 3.9, JDK 21)
> @echo; echo "Крок 2. Збірка mvn clean package. Очікувано: Building jar, BUILD SUCCESS"
> $(T)/mvn.sh clean package | grep -E 'Compiling|copy-dependencies|Building jar|BUILD'

$(JAR):
> @$(MAKE) --no-print-directory build

manifest: $(JAR) ## маніфест JAR: Main-Class і Class-Path
> @echo; echo "Крок 3. Маніфест JAR. Очікувано: Class-Path: lib/commons-lang3-3.17.0.jar і Main-Class"
> @docker run --rm -u "$(UID_GID)" -v "$$PWD":/w -w /w --entrypoint sh $(IMG) -c \
>   'mkdir /tmp/m && cd /tmp/m && jar xf /w/$(JAR) META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF' | tr -d '\r' | grep -E 'Class-Path|Main-Class'

run: $(JAR) ## java -jar: JVM, ОС, метод залежності, кирилиця
> @echo; echo "Крок 4. Запуск артефакту. Очікувано: 4 рядки, останній Привіт зі збірки!"
> $(T)/java.sh -jar $(JAR)

nolib: $(JAR) ## помилка NoClassDefFoundError без target/lib
> @echo; echo "Крок 5. JAR без теки lib. Очікувано: NoClassDefFoundError: org/apache/commons/lang3/StringUtils"
> @rm -rf target/nolib && mkdir -p target/nolib && cp $(JAR) target/nolib/
> $(T)/java.sh -jar target/nolib/env-check-1.0.jar 2>&1 | grep -E 'OS:|NoClassDefFoundError' || true

encoding: $(JAR) ## кирилиця як ? при локалі POSIX
> @echo; echo "Крок 6. Локаль POSIX (навмисно). Очікувано: ?????? ?? ??????!, а з UTF-8 локаллю Привіт зі збірки!"
> docker run --rm -u "$(UID_GID)" -e LC_ALL=POSIX -v "$$PWD":/w -w /w --entrypoint java $(IMG) -jar $(JAR) | tail -1
> $(T)/java.sh -jar $(JAR) | tail -1

javap: $(JAR) ## байт-код методу greet() через javap -c
> @echo; echo "Крок 7. Байт-код greet(). Очікувано: getstatic #7, ldc #60, invokevirtual #35, return"
> docker run --rm -u "$(UID_GID)" -v "$$PWD":/w -w /w --entrypoint javap $(IMG) -c -p -cp target/classes ua.duikt.lavrishko.env.Main | sed -n '/void greet/,/return/p'

show: version build manifest run nolib encoding javap ## усе по черзі
> @echo; echo "Готово."

clean: ## видалити target/
> rm -rf target
