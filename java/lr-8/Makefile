# ЛР 8: приймальні тести модуля підрядника (варіант 2). Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help jar warm build api good build1 build2 build3 build4 build5 zones summary clean show
MVN := ../../tools/mvn-sandbox.sh
IMG := maven:3.9-eclipse-temurin-21
VJAR ?= /m/repository/ua/vendor/vendor/1.0/vendor-1.0.jar
# з виводу Maven лишаємо підсумок і назви тестів, що впали
SHORT := grep -E '<<< (FAILURE|ERROR)!$$|^org\.opentest4j\.AssertionFailedError|^\[(INFO|ERROR)\] Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$$' | grep -v ' in ua.duikt' | sed -E 's/^\[ERROR\] ua\.duikt\.lavrishko\.lr8\.OverduePolicyTest\.//; s/ -- Time elapsed.*//'

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

jar: ## встановити jar підрядника в кеш Maven, якщо його там ще немає
> @test -f vendor/v2-vendor.jar || { echo "Немає vendor/v2-vendor.jar. Візьміть його в Classroom: ЛР №8, вкладення v2-vendor.jar (варіант 2), і покладіть у теку vendor/."; exit 1; }
> @if docker volume inspect duikt-m2 >/dev/null 2>&1 && docker run --rm --network none -v duikt-m2:/m:ro $(IMG) test -f $(VJAR); then echo "Jar підрядника вже є в кеші Maven."; else echo "Встановлення jar підрядника в кеш Maven (один раз, команда лише копіює файл)"; echo "> mvn install:install-file -Dfile=v2-vendor.jar -DgroupId=ua.vendor -DartifactId=vendor -Dversion=1.0 -Dpackaging=jar"; (cd vendor && ../../../tools/mvn.sh -q install:install-file -Dfile=v2-vendor.jar -DgroupId=ua.vendor -DartifactId=vendor -Dversion=1.0 -Dpackaging=jar) 2>&1 | { grep -v copy_reference_file || true; }; fi

warm: jar ## один раз з мережею докачати плагіни й JUnit у кеш (код підрядника не виконується)
> @mkdir -p target
> @../../tools/mvn.sh -q test -Dtest='OverduePolicyTest#calendarAssumptions' > target/warm.log 2>&1 || { cat target/warm.log; exit 1; }
> @echo "Кеш Maven з плагінами й JUnit готовий."

build: warm ## скомпілювати тести (docker без мережі, залежності з кешу)
> @echo; echo "Крок 1. Компіляція тестів у docker без мережі. Очікувано: без помилок"
> @echo "> mvn -q test-compile"
> @$(MVN) -q test-compile 2>&1 | { grep -v copy_reference_file || true; }
> @echo "Компіляція пройшла."

api: jar ## публічне API модуля через javap (без байт-коду)
> @echo; echo "Крок 2. Публічні сигнатури модуля підрядника (javap -public). Очікувано: OverduePolicy.check, IssueStatus, Vendor.build"
> @echo "> javap -public -cp vendor-1.0.jar ua.vendor.issues.OverduePolicy ua.vendor.issues.IssueStatus ua.vendor.issues.Vendor"
> @docker run --rm --network none -v duikt-m2:/m:ro $(IMG) javap -public -cp $(VJAR) ua.vendor.issues.OverduePolicy ua.vendor.issues.IssueStatus ua.vendor.issues.Vendor

good: build ## збірка 6: усі 35 тестів проходять
> @echo; echo "Крок 3. Збірка 6. Очікувано: Tests run: 35, Failures: 0 (її й рекомендовано прийняти)"
> @echo "> mvn test -Dbuild=6"
> @$(MVN) test -Dbuild=6 | $(SHORT)

build1: build ## збірка 1: вихідні рахуються як робочі дні
> @echo; echo "Крок 4. Збірка 1. Очікувано: 6 тестів впало, усі overdueByWorkingDays (вихідні рахуються як робочі)"
> @echo "> mvn test -Dbuild=1"
> @$(MVN) test -Dbuild=1 | $(SHORT) || true

build2: build ## збірка 2: дедлайн сьогодні вважається простроченим
> @echo; echo "Крок 5. Збірка 2. Очікувано: 4 впало (дедлайн сьогодні не вважається вчасним)"
> @echo "> mvn test -Dbuild=2"
> @$(MVN) test -Dbuild=2 | $(SHORT) || true

build3: build ## збірка 3: ігнорує done
> @echo; echo "Крок 6. Збірка 3. Очікувано: 3 впало, doneIssueIsOnTrack (прапорець done ігнорується)"
> @echo "> mvn test -Dbuild=3"
> @$(MVN) test -Dbuild=3 | $(SHORT) || true

build4: build ## збірка 4: для ESCALATE потрібно 3 дні замість 2
> @echo; echo "Крок 7. Збірка 4. Очікувано: 7 впало, overdueByWorkingDays (поріг ESCALATE зсунуто)"
> @echo "> mvn test -Dbuild=4"
> @$(MVN) test -Dbuild=4 | $(SHORT) || true

build5: build ## збірка 5: дата за UTC, не за київським часом
> @echo; echo "Крок 8. Збірка 5. Очікувано: 3 впало: kyivMidnightSummer, dstDayAfterMidnight, kyivDateEvenWhenClockIsInUtc"
> @echo "> mvn test -Dbuild=5"
> @$(MVN) test -Dbuild=5 | $(SHORT) || true

zones: build ## той самий результат в іншому поясі комп'ютера
> @echo; echo "Крок 9. Збірка 5 з поясом комп'ютера Pacific/Kiritimati (UTC+14). Очікувано: ті самі 3 тести"
> @echo "> mvn test -Dbuild=5 -DargLine=-Duser.timezone=Pacific/Kiritimati"
> @$(MVN) test -Dbuild=5 -DargLine=-Duser.timezone=Pacific/Kiritimati | $(SHORT) || true
> @echo; echo "Збірка 6 з поясом America/Los_Angeles. Очікувано: Failures: 0"
> @echo "> mvn test -Dbuild=6 -DargLine=-Duser.timezone=America/Los_Angeles"
> @$(MVN) test -Dbuild=6 -DargLine=-Duser.timezone=America/Los_Angeles | $(SHORT)

summary: ## збережене зведення всіх прогонів (results/summary.txt)
> @echo; echo "Крок 10. Зведення: звичайний запуск, два пояси, випадковий порядок методів"
> @cat results/summary.txt

clean: ## прибрати target/
> @rm -rf target

show: jar build api good build1 build2 build3 build4 build5 zones summary ## усе по черзі
> @echo; echo "Готово."
