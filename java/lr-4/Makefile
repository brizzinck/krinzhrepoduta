# ЛР 4: абстрактні класи й інтерфейси, OCP, ISP, DIP (звіти: текст, CSV, HTML). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build run isp paged-ok evidence di clean show
T := ../../tools
P := ua.duikt.lavrishko.reports
SNAP := snapshots/stage1_three_formats/ua/duikt/lavrishko/reports
SRC := src/main/java/ua/duikt/lavrishko/reports

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

target/reports.jar: pom.xml $(shell find src -name '*.java')
> $(T)/mvn.sh -q package

build: target/reports.jar ## зібрати проєкт (Maven у docker) у target/

run: build ## Крок 1: шість конфігурацій у Main
> @echo; echo "Крок 1. Main: шість конфігурацій (текст, CSV, HTML зі сторінками, Markdown). Очікувано: «Конфігурація 1» ... «Конфігурація 6 (нова)»"
> $(T)/java.sh -cp target/classes $(P).app.Main

isp: build ## Крок 2: CsvFormat не передати в PagedReportService (помилка javac)
> @echo; echo "Крок 2. CsvFormat у PagedReportService. Очікувано: incompatible types: CsvFormat cannot be converted to PagedFormat"
> @mkdir -p target/ce
> $(T)/javac.sh -encoding UTF-8 -d target/ce -cp target/classes compile-errors/E1_csv_paged/*.java; echo "(код завершення: $$?)"

paged-ok: build ## Крок 3: TextFormat у PagedReportService компілюється
> @echo; echo "Крок 3. TextFormat у PagedReportService. Очікувано: без помилок, код завершення 0"
> @mkdir -p target/ce
> $(T)/javac.sh -encoding UTF-8 -d target/ce -cp target/classes compile-errors/E1b_text_paged/*.java; echo "(код завершення: $$?)"

evidence: ## Крок 4: ReportService не змінився після додавання MarkdownFormat
> @echo; echo "Крок 4. Стан до MarkdownFormat (snapshots) проти поточного коду. Очікувано: відрізняються лише Main і новий MarkdownFormat, хеші ReportService однакові"
> diff -rq snapshots/stage1_three_formats src/main/java || true
> sha256sum $(SNAP)/service/ReportService.java $(SRC)/service/ReportService.java
> git diff --no-index --stat $(SNAP)/app/Main.java $(SRC)/app/Main.java || true

di: ## Крок 5: у сервісах немає new і імпортів конкретних форматів
> @echo; echo "Крок 5. Пошук new та імпортів конкретних класів у service/. Очікувано: лише імпорти інтерфейсів ReportFormat, PagedFormat, DataSource"
> grep -rn "new \|import $(P).format\|import $(P).source" $(SRC)/service

clean: ## видалити тимчасові файли (target/ce)
> rm -rf target/ce

show: build run isp paged-ok evidence di clean ## усе по черзі
> @echo; echo "Готово."
