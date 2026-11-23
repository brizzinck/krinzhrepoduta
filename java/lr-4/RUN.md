# ЛР 4: запуск і показ

Потрібні Docker і git. Усі команди з теки `java/lr-4`. Збірка йде в Maven і JDK 21 у docker (`../../tools/*.sh`), результати лише в `target/`, файли репозиторію `make` не змінює.

## Пайплайн

```sh
cd java/lr-4   # шлях відносно кореня репозиторію
make build      # mvn package, target/reports.jar
make run        # Main: шість конфігурацій
make isp        # CsvFormat не передати в PagedReportService
make paged-ok   # TextFormat передати можна
make evidence   # ReportService не змінився після MarkdownFormat
make di         # у сервісах немає new
```

`make show` виконує все по черзі й видаляє тимчасові файли, `make help` показує цілі.

## Кроки

- `make build`: `pom.xml`, `src/main/java/ua/duikt/lavrishko/reports` (`model`, `format`, `source`, `service`, `app`).
- `make run`: клас `ua.duikt.lavrishko.reports.app.Main`, дані з `data/rentals.csv`.
- `make isp`: `javac` на `compile-errors/E1_csv_paged/CsvPaged.java` (`new PagedReportService(new CsvFormat(), source)`).
- `make paged-ok`: `javac` на `compile-errors/E1b_text_paged/TextPaged.java` (той самий виклик із `TextFormat`).
- `make evidence`: порівняння `snapshots/stage1_three_formats` (код до `MarkdownFormat`) із `src/main/java` через `diff -rq`, `sha256sum` і `git diff --no-index --stat`.
- `make di`: `grep` по `src/main/java/.../service`.

## Що показати

1. `make run`: шість блоків від `Конфігурація 1: текст + дані в пам'яті` до `Конфігурація 6 (нова): Markdown + дані з файлу`. Один сервіс, різні формати й джерела; CSV коректно екранує коми й лапки, HTML розбито на три сторінки (`<!-- Сторінка 1 з 3 -->`).
2. `make isp`: `error: incompatible types: CsvFormat cannot be converted to PagedFormat`, код 1. Формат без сторінок компілятор не пропускає в сервіс зі сторінками.
3. `make paged-ok`: порожній вивід і `(код завершення: 0)`. Для `TextFormat`, який реалізує `PagedFormat`, той самий виклик коректний.
4. `make evidence`: `diff -rq` називає лише `app/Main.java` і новий `MarkdownFormat.java`; два хеші `ReportService.java` однакові (`967146dad637...cb1645`); `Main.java` +5 рядків. Сервіс не змінено.
5. `make di`: у `service/` лише рядки `import` інтерфейсів `ReportFormat`, `PagedFormat`, `DataSource`, жодного `new`. Усі об'єкти створює `Main`.

## Звідки що береться

- `data/rentals.csv`: вхідні дані для `CsvFileDataSource`.
- `snapshots/stage1_three_formats`: копія коду до додавання `MarkdownFormat`.
- `compile-errors/`: короткі програми з хибним і правильним викликом сервісу.
- `evidence.sh` і `compile-errors/run.sh` записують ті самі докази у `results/` (їх вставлено в звіт).
