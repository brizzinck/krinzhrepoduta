# ПР 7: SAST (Semgrep) і SCA (Trivy) над ЛР 3 і ЛР 6, оцінка за CVSS. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: step6 step7 step8 step9 help results cvss semgrep-lr3 semgrep-lr6 scan sca m2 db trivy-lr3 trivy-lr6 whatif-lr3 whatif-lr6 open down clean clean-all show
# префікс імен контейнерів і том з кешем Maven (спільний для збірок)
C ?= cw-pr7
M2 ?= cw-pr7-m2
SEMGREP_IMG := semgrep/semgrep:1.186.0
TRIVY_IMG := aquasec/trivy:0.76.2
MVN_IMG := maven:3.9-eclipse-temurin-21
export C M2 MVN_IMG TRIVY_IMG
IDS := -u "$$(id -u):$$(id -g)"
# Python у docker, на хості нічого не встановлюється; -I вимикає сторонні модулі
PY := docker run --rm $(IDS) -v "$$PWD":/w -w /w python:3.12-slim python -I
# правила Semgrep із публічного реєстру, без входу в акаунт
RULES := --config p/java --config p/security-audit --config p/secrets --config p/owasp-top-ten
# версії, які виправляють Tomcat, Jackson і log4j (розділ 7 звіту)
PROPS := tomcat.version=10.1.59,jackson-bom.version=2.21.7,log4j2.version=2.25.5

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

results: ## збережені результати зі звіту (results/): версії, Semgrep, Trivy
> @echo; echo "Крок 1. Збережені результати, на яких складено звіт. Очікувано: Semgrep 5 і 2 знахідки, Trivy 89 (15 Critical) для ЛР 3 і 21 для ЛР 6"
> @grep -E '^(дата запуску|образ)' results/versions.txt | sed 's/^/  /'
> @awk '/Vulnerability DB/{n="база Trivy"} /Java DB/{n="Java DB"} /UpdatedAt/{print "  " n ", оновлено: " $$2 " " substr($$3,1,8) " UTC"}' results/versions.txt
> @sed -n '/^## Semgrep/,/^## Пакети/p' results/summary.md | sed '$$d' | sed 's/^## //' | sed 's/^## //'

cvss: ## CVSS v3.1: оцінки таблиць 4 і 5 звіту (scripts/cvss31.py) і самоперевірка за звітами Trivy
> @echo; echo "Крок 2. Оцінки CVSS з розділу 6 звіту. Очікувано: 8.2 і 5.4 для SQL-ін'єкції, 9.4 і 7.4 для INSERT, 7.5 і 5.1 для смаглінгу, 0.0 для CVE-2025-24813 і BCrypt"
> @echo "SQL-ін'єкція search, база і з MAV:L/CR:L (таблиця 5, перший рядок):"
> @$(PY) scripts/cvss31.py CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:L --env MAV:L/CR:L
> @echo "SQL-ін'єкція INSERT, :64 (після таблиці 6):"
> @$(PY) scripts/cvss31.py CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:L --env MAV:L/CR:L
> @echo "CVE-2026-24880, Tomcat, смаглінг, MAV:L/MAC:H:"
> @$(PY) scripts/cvss31.py CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:H/A:N --env MAV:L/MAC:H
> @echo "CVE-2025-24813, Tomcat, PUT, MC:N/MI:N/MA:N:"
> @$(PY) scripts/cvss31.py CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H --env MC:N/MI:N/MA:N
> @echo "CVE-2025-22228, BCrypt, MC:N/MI:N:"
> @$(PY) scripts/cvss31.py CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N --env MC:N/MI:N
> @echo; echo "Крок 3. Самоперевірка скрипта за векторами зі збережених звітів Trivy. Очікувано: 56 пар, 2 розбіжності (бази GHSA на 0,1 вищі: 9.1 і 9.7), решта збігається"
> @mkdir -p target; $(PY) scripts/cvss31.py --selftest results/trivy-lr3-jar.json results/trivy-lr6-jar.json > target/selftest.txt; rc=$$?; \
>   grep -E 'РОЗБІЖНІСТЬ|Log4Shell|перевірено' target/selftest.txt; \
>   { [ $$rc -le 1 ] && grep -q 'розбіжностей: 2$$' target/selftest.txt; } || { echo "Самоперевірка CVSS дала несподіваний результат (очікувалось 2 розбіжності), див. target/selftest.txt"; exit 1; }

semgrep-lr3: ## SAST: Semgrep CE над ЛР 3 (потрібен інтернет для правил з реєстру)
> @echo; echo "Крок 4. Semgrep над ../lr-3/app, код підключено лише для читання. Очікувано: 36 файлів, 5 знахідок (3 ERROR, 2 WARNING), SQL-конкатенація в VulnerableDemoController і BCrypt-хеш"
> @$(MAKE) --no-print-directory scan LR=lr-3 SHORT=lr3

semgrep-lr6: ## SAST: Semgrep CE над ЛР 6 (потрібен інтернет для правил з реєстру)
> @echo; echo "Крок 5. Semgrep над ../lr-6/app. Очікувано: 19 файлів, 2 знахідки (ERROR), обидві detected-bcrypt-hash в UserStore.java"
> @$(MAKE) --no-print-directory scan LR=lr-6 SHORT=lr6

# внутрішня ціль: LR=lr-3, SHORT=lr3
scan:
> @mkdir -p target
> @d="../$(LR)/app"; targets="src/main src/test $$(cd $$d && ls | grep -vE '^(src|target|logs)$$' | tr '\n' ' ')"; \
>   docker run --rm --name $(C)-semgrep-$(SHORT) $(IDS) -e HOME=/tmp -v "$$PWD/$$d":/src:ro -v "$$PWD/target":/out -w /src $(SEMGREP_IMG) \
>   semgrep scan --metrics=off --no-git-ignore --exclude target --exclude logs $(RULES) --json-output /out/semgrep-$(SHORT).json $$targets \
>   > target/semgrep-$(SHORT).txt 2>&1 || true
> @[ -s target/semgrep-$(SHORT).json ] || { tail -n 8 target/semgrep-$(SHORT).txt; echo "Semgrep не дав результату: потрібен інтернет, щоб отримати правила p/java, p/security-audit, p/secrets, p/owasp-top-ten."; exit 1; }
> @grep -E '^Ran [0-9]+ rules' target/semgrep-$(SHORT).txt
> @$(PY) scripts/show.py semgrep target/semgrep-$(SHORT).json results/semgrep-$(SHORT).json

m2:
> @docker volume inspect $(M2) >/dev/null 2>&1 || { docker volume create $(M2) >/dev/null && docker run --rm -v $(M2):/m alpine:3 chown "$$(id -u):$$(id -g)" /m && echo "Створено том $(M2) для кешу Maven."; }

target/build/%/target/app.jar: | m2
> @echo "Збірка $* у docker (Maven, JDK 21), копія без target: перший раз Maven завантажує залежності."
> @rm -rf target/build/$* && mkdir -p target/build/$*
> @(cd ../$*/app && tar --exclude=./target --exclude=./logs -cf - .) | tar -xf - -C target/build/$*
> @docker run --rm --name $(C)-mvn-$* $(IDS) -e LANG=C.UTF-8 -v $(M2):/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 \
>   -v "$$PWD/target/build/$*":/w -w /w $(MVN_IMG) mvn -B -q -Duser.home=/var/maven -DskipTests package
> @echo "app.jar $*: $$(wc -c < target/build/$*/target/app.jar) байт"

target/cache/trivy/java-db/trivy-java.db:
> @mkdir -p target/cache/trivy
> @echo "Завантаження баз Trivy (близько 1,1 ГБ, перший раз кілька хвилин)."
> @docker run --rm --name $(C)-trivy-db $(IDS) -e HOME=/tmp -v "$$PWD/target/cache/trivy":/cache $(TRIVY_IMG) --quiet image --download-db-only --cache-dir /cache
> @docker run --rm --name $(C)-trivy-javadb $(IDS) -e HOME=/tmp -v "$$PWD/target/cache/trivy":/cache $(TRIVY_IMG) --quiet image --download-java-db-only --cache-dir /cache

db: target/cache/trivy/java-db/trivy-java.db ## показати бази Trivy в target/cache/trivy (завантажити, якщо їх ще немає)
> @echo; echo "Бази Trivy в target/cache/trivy. Очікувано: Version 0.76.2 і дати оновлення бази вразливостей та Java DB (при першому запуску спершу завантаження)"
> @docker run --rm --name $(C)-trivy-ver $(IDS) -e HOME=/tmp -v "$$PWD/target/cache/trivy":/cache $(TRIVY_IMG) version --cache-dir /cache | awk '/^Version/{print "  Trivy " $$2} /Vulnerability DB/{n="база вразливостей"} /Java DB/{n="Java DB"} /UpdatedAt/{print "  " n ", оновлено: " $$2 " " substr($$3,1,8) " UTC"}'

trivy-lr3: step6 target/build/lr-3/target/app.jar target/cache/trivy/java-db/trivy-java.db ## SCA: Trivy над app.jar і pom.xml ЛР 3
> @$(MAKE) --no-print-directory sca LR=lr-3 SHORT=lr3

step7:
> @echo; echo "Крок 7. Trivy над app.jar і pom.xml ЛР 6. Очікувано: 21 знахідка, 7/6/8/0, головні Tomcat 10.1.55 і spring-webmvc 6.2.19 (числа можуть бути більшими, якщо база новіша)"
trivy-lr6: step7 target/build/lr-6/target/app.jar target/cache/trivy/java-db/trivy-java.db ## SCA: Trivy над app.jar і pom.xml ЛР 6
> @$(MAKE) --no-print-directory sca LR=lr-6 SHORT=lr6

step6:
> @echo; echo "Крок 6. Trivy над app.jar і pom.xml ЛР 3. Очікувано: 89 знахідок, 15/31/29/14, головні Tomcat 10.1.31 і thymeleaf; jar і pom.xml збігаються (числа можуть бути більшими, якщо база новіша за звітну від 2026-11-22)"

# внутрішня ціль: LR=lr-3, SHORT=lr3
sca:
> @mkdir -p target/pomonly/$(LR)
> @cp target/build/$(LR)/pom.xml target/pomonly/$(LR)/pom.xml
> @docker run --rm --name $(C)-trivy-$(SHORT)-jar $(IDS) -e HOME=/tmp/h -v "$$PWD/target/build/$(LR)/target":/scan:ro -v "$$PWD/target":/out \
>   -v "$$PWD/target/cache/trivy":/cache -w /scan $(TRIVY_IMG) --quiet rootfs --offline-scan --scanners vuln --format json \
>   --output /out/trivy-$(SHORT)-jar.json --cache-dir /cache /scan/app.jar
> @docker run --rm --name $(C)-trivy-$(SHORT)-pom $(IDS) -e HOME=/tmp/h -v $(M2):/tmp/h/.m2:ro -v "$$PWD/target/pomonly/$(LR)":/scan:ro -v "$$PWD/target":/out \
>   -v "$$PWD/target/cache/trivy":/cache -w /scan $(TRIVY_IMG) --quiet fs --offline-scan --scanners vuln --format json \
>   --output /out/trivy-$(SHORT)-pom.json --cache-dir /cache /scan
> @echo "app.jar:"
> @$(PY) scripts/show.py trivy target/trivy-$(SHORT)-jar.json results/trivy-$(SHORT)-jar.json
> @echo "pom.xml і app.jar:"
> @$(PY) scripts/show.py same target/trivy-$(SHORT)-jar.json target/trivy-$(SHORT)-pom.json

step8:
> @echo; echo "Крок 8. Копія ЛР 3 з оновленими залежностями. Очікувано: parent 3.5.16 дає 21 знахідку (7/6/8/0), з трьома властивостями 2 (2/0/0/0), тести 76 без помилок"
whatif-lr3: step8 target/cache/trivy/java-db/trivy-java.db | m2 ## «що, якщо» для ЛР 3: parent 3.5.16, потім ще й версії Tomcat, Jackson, log4j
> @scripts/whatif.sh lr-3 3.5.16 - 3.5.16 results/trivy-lr3-whatif-3.5.16.json
> @scripts/whatif.sh lr-3 3.5.16 $(PROPS) 3.5.16-props results/trivy-lr3-whatif-3.5.16-props.json

step9:
> @echo; echo "Крок 9. Копія ЛР 6 з трьома оновленими властивостями. Очікувано: 2 знахідки (2/0/0/0), тести 18 без помилок"
whatif-lr6: step9 target/cache/trivy/java-db/trivy-java.db | m2 ## «що, якщо» для ЛР 6: версії Tomcat, Jackson, log4j
> @scripts/whatif.sh lr-6 3.5.16 $(PROPS) 3.5.16-props results/trivy-lr6-whatif-3.5.16-props.json

open: ## відкрити звіт у PDF і слайди (потрібна програма перегляду)
> @setsid -f xdg-open report.pdf >/dev/null 2>&1 && echo "Відкрито: report.pdf" || echo "Відкрий вручну: report.pdf"
> @setsid -f xdg-open slides/slides.html >/dev/null 2>&1 && echo "Відкрито: slides/slides.html" || echo "Відкрий вручну: slides/slides.html"

down: ## зупинити контейнери ПР 7, якщо вони лишились
> @ids="$$(docker ps -aq --filter name=^$(C)-)"; [ -z "$$ids" ] || docker rm -f $$ids >/dev/null
> @echo "Контейнерів $(C)-* немає."

clean: down ## видалити результати й копії в target/, лишити кеш баз Trivy і том Maven
> @[ ! -d target ] || find target -mindepth 1 -maxdepth 1 ! -name cache -exec rm -rf {} +
> @echo "Результати й копії видалено. Кеш баз Trivy (target/cache) і том $(M2) лишено, щоб повторні запуски були швидкими; повністю: make clean-all."

clean-all: down ## прибрати все: target/ разом з базами Trivy і том Maven
> @rm -rf target
> @docker volume rm $(M2) >/dev/null 2>&1 || true
> @echo "target/ і том $(M2) видалено."

show: results cvss semgrep-lr3 semgrep-lr6 trivy-lr3 trivy-lr6 whatif-lr3 whatif-lr6 clean ## усе по черзі
> @echo; echo "Готово."
