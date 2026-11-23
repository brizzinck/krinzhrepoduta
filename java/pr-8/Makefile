# ПР 8: JUnit, параметризовані тести, Mockito (варіант 2). Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help build test1 break1 test2 break2 test3 break3 agent final clean show
MVN := ../../tools/mvn.sh
# зламані копії проєкту лежать у target/demo, файли репозиторію не змінюються
DEMO := target/demo
mvnd = (cd $(DEMO) && ../../../../tools/mvn.sh $(1))
SHORT := grep -E 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$$|^\[ERROR\]   [A-Za-z]|BUILD (SUCCESS|FAILURE)'

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## скомпілювати проєкт (Maven у docker)
> @echo; echo "Крок 1. Компіляція. Очікувано: BUILD SUCCESS"
> @echo "> mvn -q test-compile"
> @$(MVN) -q test-compile 2>&1 | { grep -v copy_reference_file || true; }
> @echo "Компіляція пройшла."

test1: build ## дослід 1: SprintTest, три тести Given-When-Then
> @echo; echo "Крок 2. SprintTest. Очікувано: Tests run: 3, Failures: 0"
> @echo "> mvn test -Dtest=SprintTest"
> @$(MVN) test -Dtest=SprintTest 2>&1 | $(SHORT)

break1: build ## дослід 1: CAPACITY = 30, падає addsTaskWhenItFits
> @echo; echo "Крок 3. У копії проєкту CAPACITY = 30. Очікувано: Failures: 1, SprintTest.addsTaskWhenItFits:19"
> @rm -rf $(DEMO) && mkdir -p $(DEMO) && cp -r pom.xml pom-without-agent.xml src $(DEMO)/
> @sed -i 's/CAPACITY = 40/CAPACITY = 30/' $(DEMO)/src/main/java/ua/duikt/lavrishko/pr8/Sprint.java
> @echo "> mvn test -Dtest=SprintTest   (CAPACITY = 30)"
> @$(call mvnd,test -Dtest=SprintTest) 2>&1 | $(SHORT) || true

test2: build ## дослід 2: SprintBoundaryTest, межі (7 прогонів)
> @echo; echo "Крок 4. SprintBoundaryTest, межові значення. Очікувано: Tests run: 7, Failures: 0"
> @echo "> mvn test -Dtest=SprintBoundaryTest"
> @$(MVN) test -Dtest=SprintBoundaryTest 2>&1 | $(SHORT)

break2: build ## дослід 2: <= замінено на <, падає рядок [3]
> @echo; echo "Крок 5. У копії <= замінено на <. Очікувано: Failures: 1, boundariesInsideRange:23 (рядок [3], сума рівно 40)"
> @rm -rf $(DEMO) && mkdir -p $(DEMO) && cp -r pom.xml pom-without-agent.xml src $(DEMO)/
> @sed -i 's/current + points <= CAPACITY/current + points < CAPACITY/' $(DEMO)/src/main/java/ua/duikt/lavrishko/pr8/Sprint.java
> @echo "> mvn test -Dtest=SprintBoundaryTest   (<)"
> @$(call mvnd,test -Dtest=SprintBoundaryTest) 2>&1 | $(SHORT) || true

test3: build ## дослід 3: IssueServiceTest, стаб і мок
> @echo; echo "Крок 6. IssueServiceTest зі стабом і моком. Очікувано: Tests run: 2, Failures: 0"
> @echo "> mvn test -Dtest=IssueServiceTest"
> @$(MVN) test -Dtest=IssueServiceTest 2>&1 | $(SHORT)

break3: build ## дослід 3: виклик send закоментовано, Wanted but not invoked
> @echo; echo "Крок 7. У копії закоментовано notifier.send. Очікувано: Failures: 2, Wanted but not invoked, zero interactions"
> @rm -rf $(DEMO) && mkdir -p $(DEMO) && cp -r pom.xml pom-without-agent.xml src $(DEMO)/
> @sed -i 's|^        notifier.send(login, text);|        // notifier.send(login, text);|' $(DEMO)/src/main/java/ua/duikt/lavrishko/pr8/IssueService.java
> @echo "> mvn test -Dtest=IssueServiceTest   (без send)"
> @$(call mvnd,test -Dtest=IssueServiceTest) > $(DEMO)/out.txt 2>&1 || true; grep -E '^\[ERROR\]   [A-Za-z]' $(DEMO)/out.txt; grep -m1 -A6 'Wanted but not invoked' $(DEMO)/out.txt; grep -E 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$$' $(DEMO)/out.txt

agent: build ## попередження про агента Mockito без argLine
> @echo; echo "Крок 8. pom без argLine. Очікувано: Mockito is currently self-attaching, A Java agent has been loaded dynamically"
> @echo "> mvn -f pom-without-agent.xml test -Dtest=IssueServiceTest"
> @$(MVN) -f pom-without-agent.xml test -Dtest=IssueServiceTest 2>&1 | grep -E 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$$|self-attaching|has been loaded dynamically' | cut -c1-110
> @echo; echo "Той самий тест із pom.xml (argLine з -javaagent). Очікувано: попереджень про агента немає"
> @echo "> mvn test -Dtest=IssueServiceTest"
> @$(MVN) test -Dtest=IssueServiceTest 2>&1 | grep -cE 'self-attaching|has been loaded dynamically' | sed 's/^/Рядків про агента: /' || true

final: build ## усі тести разом
> @echo; echo "Крок 9. Усі тести. Очікувано: Tests run: 12, Failures: 0, BUILD SUCCESS"
> @echo "> mvn test"
> @$(MVN) test 2>&1 | $(SHORT)

clean: ## прибрати target/
> @rm -rf target

show: build test1 break1 test2 break2 test3 break3 agent final clean ## усе по черзі
> @echo; echo "Готово."
