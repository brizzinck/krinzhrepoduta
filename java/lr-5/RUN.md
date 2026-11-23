# ЛР 5: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/lr-5`, сама утиліта лежить у `importer/`. Збірка йде в Maven і JDK 21 у docker (`../../../tools/*.sh`), звіти й тимчасові файли пишуться в `importer/target/`, файли репозиторію `make` не змінює.

## Пайплайн

```sh
cd java/lr-5   # шлях відносно кореня репозиторію
make build      # mvn package, importer/target/importer.jar
make correct    # коректний файл
make damaged    # пошкоджений файл
make large      # файл на 200 000 рядків
make missing    # немає вхідного файлу
make nodir      # звіт у теку, якої немає
make encoding   # файл у windows-1251
make memory     # купа 8 МБ: потокове читання проти readAllLines
make cause      # ланцюжок винятків
make verify     # незалежний перерахунок підсумків
```

`make show` виконує все по черзі й видаляє тимчасові файли, `make help` показує цілі.

## Кроки

- `make correct`, `damaged`, `large`: `java -jar target/importer.jar data/v2_*.txt target/out/report_*.txt` (клас `ua.duikt.lavrishko.importer.app.Main`, імпорт у `StockImporter`, розбір рядка в `StockItemParser`).
- `make missing`, `nodir`, `encoding`: той самий `jar` із `nofile.txt`, `/nonexistent_dir/report.txt` і `demo/v2_cp1251.txt`.
- `make memory`: `-Xmx8m` для утиліти й для `demo/ReadAllDemo.java` (читає файл через `Files.readAllLines`), потім `-Xmx64m` для `ReadAllDemo`.
- `make cause`: `demo/CauseDemo.java`, який показує `cause` для кожного виду помилки.
- `make verify`: `demo/verify.py` рахує підсумки на `decimal.Decimal` без Java; Python запускається в docker (`python:3.12-slim`).

## Що показати

1. `make correct`: `Імпортовано: 20, відхилено: 0. Загальна вартість залишків: 10409337.34.`, код 0.
2. `make damaged`: вісім рядків `Рядок N: причина`, потім `Імпортовано: 17, відхилено: 8. Загальна вартість залишків: 11627064.32.`, код 0. Нижче показано файл звіту `target/out/report_damaged.txt` з тими самими рядками й підсумком.
3. `make large`: перші 20 відхилених, `... і ще 77 відхилених рядків (повний перелік у звіті)`, `Імпортовано: 199903, відхилено: 97. Загальна вартість залишків: 124872432674.03.`
4. `make missing`: `Помилка: файл nofile.txt не знайдено` і `причина: NoSuchFileException: nofile.txt`, код 1, стектрейсу немає.
5. `make nodir`: `Помилка: неможливо створити звіт /nonexistent_dir/report.txt: теки не існує`, код 1.
6. `make encoding`: `Помилка: файл demo/v2_cp1251.txt не в кодуванні UTF-8 (рядок 1)`, код 1. Команда `ls` повідомляє, що файла звіту немає: помилка в першому рядку (кодування чи заголовок) не створює файла звіту.
7. `make memory`: з `-Xmx8m` утиліта дає `Імпортовано: 199903, відхилено: 97`, код 0; `ReadAllDemo` падає з `java.lang.OutOfMemoryError: Java heap space` у `Files.readAllLines`, код 1; з `-Xmx64m` `Прочитано рядків: 200001`.
8. `make cause`: для `A-1;десять;5` причина `NumberFormatException`, для `A-1;0;5` причина `InvalidStockItemException`, для `A-1;5;12,50` причини немає; далі ланцюжок `RecordFormatException` з `Caused by: NumberFormatException`.
9. `make verify`: `20 / 10409337.34`, `17 і 8 / 11627064.32`, `199903 і 97 / 124872432674.03`, тобто ті самі числа, що вивела утиліта.

## Звідки що береться

- `importer/data/v2_correct.txt`, `v2_damaged.txt`, `v2_large.txt`: вхідні файли варіанта 2 (артикул;кількість;ціна).
- `importer/demo/v2_cp1251.txt`: копія коректного файла в кодуванні windows-1251.
- `importer/demo/`: `ReadAllDemo.java`, `CauseDemo.java`, `verify.py`; вони компілюються в `importer/target/demo`.
- `importer/results/` містить ті самі виводи, що вставлені в звіт.
