---
pagetitle: "Звіт з лабораторної роботи № 5"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з лабораторної роботи № 5"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Винятки, захисне програмування та основи вводу-виводу. Утиліта імпорту даних з текстового файлу"
variant: "Варіант 2"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---

<style>
pre, pre code { overflow: visible !important; overflow-wrap: anywhere !important; }
</style>

# 1. Мета роботи

Набути навичок захисного програмування: перевіряти зовнішні дані на межі, створювати й перехоплювати власні винятки, гарантовано закривати файли та читати великі файли потоково з явним кодуванням.

# 2. Завдання за варіантом

Номер у списку групи 12, варіант 2 (12 mod 5). Консольна утиліта `java -jar importer.jar вхід вихід` читає записи `артикул;кількість;ціна` (UTF-8, перший рядок заголовок, дробова частина через крапку), відхиляє некоректні (кількість ціла й більша за 0, ціна не менша за 0) і записує звіт із кількістю позицій та загальною вартістю залишків. Проєкт: Maven, Java 21, без залежностей; пакети `domain` (`StockItem`), `parse` (`StockItemParser`), `app` (`Main`, `StockImporter`).

# 3. Де й як перевіряються дані

Дані перевіряються на межі між текстом файлу й предметною областю, під час розбору рядка. `StockItemParser.parse` перевіряє формат (порожній рядок, кількість полів), перетворює текст на типи й передає значення фабриці `StockItem.create`, яка перевіряє правила варіанта.

```java
    public StockItem parse(String line, long lineNumber) throws RecordFormatException {
        if (line.isBlank()) {
            throw new RecordFormatException(lineNumber, "порожній рядок");
        }
        // limit = -1: не відкидати порожні поля в кінці ("A-1;5;" дає 3 поля, а не 2)
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new RecordFormatException(lineNumber,
                    "очікувалось " + FIELD_COUNT + " поля, отримано " + fields.length);
        }
        String article = fields[0].strip();
        int quantity = parseQuantity(fields[1].strip(), lineNumber);
        BigDecimal price = parsePrice(fields[2].strip(), lineNumber);
        try {
            return StockItem.create(article, quantity, price);
        } catch (InvalidStockItemException e) {
            // повідомлення доменного винятку зрозуміле користувачу, а сам виняток лишається причиною
            throw new RecordFormatException(lineNumber, e.getMessage(), e);
        }
    }
```

Числа розбирають окремі методи, що перетворюють `NumberFormatException` на `RecordFormatException` з номером рядка. Конструктор `StockItem` закритий, тому створити об'єкт можна лише через `create`: некоректний залишок не може існувати. Ціну збережено як `BigDecimal`, щоб сума 200 тисяч значень була точною.

```java
    public static StockItem create(String article, int quantity, BigDecimal price)
            throws InvalidStockItemException {
        if (article == null || article.isBlank()) {
            throw new InvalidStockItemException("артикул порожній");
        }
        if (quantity <= 0) {
            throw new InvalidStockItemException("кількість " + quantity + " має бути більше 0");
        }
        Objects.requireNonNull(price, "price");
        if (price.signum() < 0) {
            throw new InvalidStockItemException("ціна " + price.toPlainString() + " не може бути від'ємною");
        }
        return new StockItem(article.strip(), quantity, price);
    }
```

# 4. Створені винятки

Три власні перевірювані винятки (успадковують `Exception`, тож компілятор змушує їх обробити). Рівень рядка відокремлено від рівня файлу, бо реакція різна: помилка рядка лише відхиляє його, помилка файлу зупиняє імпорт.

| Виняток (пакет) | Коли виникає | `cause` |
|----------|-------------|-----------|
| `InvalidStockItemException` (`domain`) | порушено правило домену: кількість, ціна, артикул | немає |
| `RecordFormatException` (`parse`) | рядок неможливо перетворити на `StockItem`; містить номер рядка | `NumberFormatException` або `InvalidStockItemException` |
| `ImportFailedException` (`app`) | імпорт неможливо продовжити: немає файлу чи доступу, помилка читання/запису | `IOException` |

Причина не губиться: кожен виняток вищого рівня отримує початковий у `cause`. Це показує допоміжна програма `demo/CauseDemo.java`:

```
> java -cp target/classes:demo/out CauseDemo
…
Ланцюжок винятків для першого рядка (клас, повідомлення, верхній кадр стека):
RecordFormatException: кількість "десять" не є цілим числом
    at StockItemParser.parseQuantity(StockItemParser.java:42)
Caused by: NumberFormatException: For input string: "десять"
```

# 5. Де винятки перехоплюються і що бачить користувач

Перехоплення в двох місцях, за принципом «хто може щось змінити». Цикл `StockImporter.importFile` ловить `RecordFormatException`: лише тут відомо, що помилка стосується одного рядка, а решту файлу треба обробити. `Main.run` ловить `ImportFailedException`: це точка входу, яка показує повідомлення й задає код завершення.

```java
            while ((line = readLine(reader, input, lineNumber + 1)) != null) {
                lineNumber++;
                try {
                    StockItem item = parser.parse(line, lineNumber);
                    summary.accept(item);
                } catch (RecordFormatException e) {
                    summary.reject();
                    writeLine(writer, "Рядок " + e.lineNumber() + ": " + e.getMessage(), output);
                    onRejected.accept(e);
                }
            }
```

```java
        } catch (ImportFailedException e) {
            System.err.println("Помилка: " + e.getMessage());
            printCause(e.getCause());
            return EXIT_IMPORT_FAILED;
        } catch (RuntimeException e) {
            // Помилка в самій програмі. Стектрейс не показуємо, але тип і повідомлення видно.
            System.err.println("Непередбачена помилка: " + e);
            return EXIT_UNEXPECTED;
        }
```

| Ситуація | Хто перехоплює | Що бачить користувач | Код |
|-------|-------|--------------|--|
| пошкоджений рядок | цикл у `StockImporter` | `Рядок N: причина`, імпорт триває | 0 |
| вхідного файлу немає | `Main.run` | `Помилка: файл … не знайдено`, `причина: NoSuchFileException` | 1 |
| звіт неможливо створити (теки немає) | `Main.run` | `Помилка: неможливо створити звіт /nonexistent_dir/report.txt: теки не існує`, `причина: NoSuchFileException` | 1 |

# 6. Закриття файлів і потокова обробка

Обидва файли відкрито в заголовку `try-with-resources`: `close()` викликається за будь-якого результату, у зворотному порядку. Кодування явне (`StandardCharsets.UTF_8`). Файл читається по одному рядку, `ImportSummary` зберігає лише лічильники й суму, тож пам'ять не залежить від розміру файлу.

```java
        // Ресурси закриваються у зворотному порядку: спершу writer, потім reader.
        try (BufferedReader reader = openInput(input);
             BufferedWriter writer = openOutput(output)) {
            // ...
            readHeader(reader, input);
            long lineNumber = 1;
            String line;
            // Потокове читання: у пам'яті завжди лише один рядок файлу.
            while ((line = readLine(reader, input, lineNumber + 1)) != null) {
            // ...
        } catch (IOException e) {
            // Сюди потрапляє лише помилка close(): writer під час закриття дописує буфер на диск.
            throw new ImportFailedException("не вдалося дописати звіт у файл " + output, e);
        }
```

Доказ потокової обробки: великий файл (200 000 рядків, ≈3,5 МБ) утиліта обробляє з купою `-Xmx8m`, а контрприклад `demo/ReadAllDemo.java` з `Files.readAllLines` за тих самих умов падає (з `-Xmx64m` працює).

```
> java -Xmx8m -jar importer.jar data/v2_large.txt results/report_large_8m.txt
…
Імпортовано: 199903, відхилено: 97. Загальна вартість залишків: 124872432674.03. Звіт записано у results/report_large_8m.txt
(код завершення: 0)

> java -Xmx8m -cp demo/out ReadAllDemo data/v2_large.txt
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	…
	at java.base/java.nio.file.Files.readAllLines(Files.java:3408)
(код завершення: 1)
```

# 7. Результати запуску

Коректний файл `v2_correct.txt`: усі 20 рядків імпортовано.

```
> java -jar importer.jar data/v2_correct.txt results/report_correct.txt
Імпортовано: 20, відхилено: 0. Загальна вартість залишків: 10409337.34. Звіт записано у results/report_correct.txt
(код завершення: 0)
```

Пошкоджений файл `v2_damaged.txt`: 25 рядків даних, 17 імпортовано, 8 відхилено з номером і причиною, решта оброблена.

```
> java -jar importer.jar data/v2_damaged.txt results/report_damaged.txt
Рядок 4: кількість 0 має бути більше 0
Рядок 7: очікувалось 3 поля, отримано 2
Рядок 10: кількість "десять" не є цілим числом
Рядок 13: ціна -10.00 не може бути від'ємною
Рядок 16: порожній рядок
Рядок 19: ціна "12,50": дробову частину треба відокремлювати крапкою
Рядок 22: очікувалось 3 поля, отримано 4
Рядок 25: кількість -2 має бути більше 0
Імпортовано: 17, відхилено: 8. Загальна вартість залишків: 11627064.32. Звіт записано у results/report_damaged.txt
(код завершення: 0)
```

Великий файл `v2_large.txt`: консоль показує перші 20 відхилених рядків, повний перелік із 97 лежить у файлі звіту.

```
> java -jar importer.jar data/v2_large.txt results/report_large.txt
Рядок 1221: ціна "12,50": дробову частину треба відокремлювати крапкою
…
... і ще 77 відхилених рядків (повний перелік у звіті)
Імпортовано: 199903, відхилено: 97. Загальна вартість залишків: 124872432674.03. Звіт записано у results/report_large.txt
(код завершення: 0)
```

Неіснуючий файл: повідомлення без стектрейсу.

```
> java -jar importer.jar nofile.txt results/report_nofile.txt
Помилка: файл nofile.txt не знайдено
  причина: NoSuchFileException: nofile.txt
(код завершення: 1)
```

Підсумки збігаються з незалежним перерахунком окремою програмою на `decimal.Decimal` (`demo/verify.py`): 20 / 10409337.34, 17 + 8 / 11627064.32, 199903 + 97 / 124872432674.03. «Кількість позицій» означає кількість імпортованих рядків, вартість дорівнює сумі «кількість × ціна»; порожній рядок вважається відхиленим (25 = 17 + 8).

# 8. Висновки

Розроблено консольну утиліту імпорту залишків товарів за варіантом 2. Дані перевіряються на межі, під час розбору рядка, а об'єкт `StockItem` неможливо створити з некоректними значеннями, бо конструктор закритий, а фабрика `create` перевіряє правила домену. Створено три власні винятки (`InvalidStockItemException`, `RecordFormatException`, `ImportFailedException`), які передають низькорівневу причину в `cause`. Помилку рядка перехоплює цикл імпортера, тож пошкоджені рядки відхиляються з номером і причиною, а помилку файлу перехоплює `Main`, і користувач бачить повідомлення без стектрейсу. Файли закриває `try-with-resources`, кодування UTF-8 вказано явно. Потокове читання дозволило обробити 200 000 рядків із купою 8 МБ, тоді як читання цілком завершилось `OutOfMemoryError`. Підсумки на всіх трьох файлах збігаються з незалежним перерахунком.

# 9. Посилання на репозиторій

Код утиліти, дані, результати запусків і звіт: <https://github.com/brizzinck/krinzhrepoduta/tree/main/java/lr-5>
