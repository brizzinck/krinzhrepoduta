---
pagetitle: "Лабораторна робота № 4"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з лабораторної роботи № 4"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Абстрактні класи та інтерфейси. Принципи OCP, ISP, DIP. Розширюваний застосунок з передачею залежностей ззовні"
variant: "Варіант 2 (Генерація звітів: текст, CSV, HTML; розбиття на сторінки; джерело даних)"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---

# Мета роботи

Опанувати проєктування абстракцій (абстрактні класи та інтерфейси) і принципи OCP, ISP, DIP з ручним впровадженням залежностей через конструктор.

# Завдання за варіантом

Варіант 2 (12 mod 5): центральний сервіс **генерації звітів**, формати текст, CSV і HTML, додаткова можливість розбиття на сторінки, друга залежність сервісу джерело даних.

# Теоретичні відомості

OCP: код відкритий для розширення й закритий для зміни. ISP: клієнт не залежить від методів, які не використовує, тому необов'язкові можливості виносять в окремі інтерфейси. DIP: верхній рівень залежить від абстракцій; об'єкт отримує залежності ззовні (через конструктор), а зв'язує їх одне місце, тут `Main`.

# Хід виконання роботи

![Рисунок 1. Діаграма класів. Суцільна лінія з порожнім трикутником: успадкування, пунктир з порожнім трикутником: реалізація інтерфейсу, фіолетовий пунктир: залежність сервісу, зелений пунктир: створення в `Main`](diagrams/classes.png){width=100%}

## Абстрактний клас чи інтерфейс

| Тип | Вид | Чому |
|---|---|---|
| `ReportFormat`, `DataSource` | інтерфейси | ролі, від яких залежить сервіс; сервісу не потрібна спільна основа реалізацій, а клас може виконувати кілька ролей (`HtmlFormat` водночас `ReportFormat` і `PagedFormat`) |
| `PagedFormat` | інтерфейс (розширює `ReportFormat`) | можливість лише частини форматів |
| `AbstractReportFormat` | абстрактний клас | лише в абстрактному класі можна зробити спільний порядок «заголовок, рядки, підсумок» `final`-шаблоном і сховати кроки як `protected` |
| `TextFormat`, `CsvFormat`, `HtmlFormat`, `MarkdownFormat`, `InMemoryDataSource`, `CsvFileDataSource` | класи | лише відмінності: заголовок, форма рядка, підсумок; джерело таблиці |

Інтерфейс обирано, коли треба описати роль чи можливість для різних класів; абстрактний клас, коли реалізації мають спільний код. Обидва підходи поєднано: `AbstractReportFormat implements ReportFormat`.

## Спільна поведінка без дублювання

```java
public abstract class AbstractReportFormat implements ReportFormat {
    @Override
    public final String render(Table table) {
        StringBuilder out = new StringBuilder(header(table));
        for (List<String> row : table.rows()) {
            out.append(formatRow(table, row)).append('\n');
        }
        out.append(footer(table));
        return out.toString();
    }
    protected abstract String header(Table table);
    protected abstract String formatRow(Table table, List<String> row);
    protected String footer(Table table) { return ""; }
    // спільний цикл сторінок; формат передає лише позначку «Сторінка N з M»
    protected final List<String> renderPagesWith(Table table, int pageSize,
            BiFunction<Integer, Integer, String> marker) { /* Pagination.split + render + marker */ }
}
```

Шаблонний метод записано один раз; формати реалізують лише різне; сторінки розбиває один `Pagination.split`, а цикл по сторінках записано один раз у `renderPagesWith`, у `TextFormat` і `HtmlFormat` лишилася тільки позначка.

## Розділення інтерфейсів (ISP)

```java
public interface ReportFormat { String render(Table table); }
public interface PagedFormat extends ReportFormat {
    List<String> renderPages(Table table, int pageSize);
}
```

`PagedFormat` реалізують лише `TextFormat` і `HtmlFormat`. У `CsvFormat` немає публічного методу сторінок і заглушки; захищений помічник `renderPagesWith` спільний для форматів зі сторінками й до контракту не входить. Окремий `PagedReportService` приймає `PagedFormat`, тому помилкове використання відхиляє компілятор:

```text
> javac -encoding UTF-8 -cp target/classes compile-errors/E1_csv_paged/*.java
compile-errors/E1_csv_paged/CsvPaged.java:11: error: incompatible types: CsvFormat cannot be converted to PagedFormat
        new PagedReportService(new CsvFormat(), source);
                               ^
…
1 error
```

## Передача залежностей (DIP)

```java
public class ReportService {
    private final ReportFormat format;
    private final DataSource source;

    public ReportService(ReportFormat format, DataSource source) {
        this.format = format;
        this.source = source;
    }
    public String generate() { return format.render(source.load()); }
}
```

Сервіс отримує обидві залежності через конструктор як інтерфейси й не містить `new`. Усі об'єкти створює й з'єднує `Main` (фрагмент):

```java
DataSource memory = new InMemoryDataSource(new Table("Парк авто", ...));
DataSource file = new CsvFileDataSource(csv, "Оренди за тиждень");
new ReportService(new TextFormat(), memory).generate();   // конф. 1
new ReportService(new CsvFormat(), file).generate();      // конф. 2
new PagedReportService(new HtmlFormat(), file).generatePages(3);
```

Формат і джерело замінюються незалежно: конфігурації 1 і 4 мають той самий текстовий формат і різні джерела, а 2 і 4 той самий файл і різні формати. Вивід етапу 1 (CSV з коректним екрануванням коми й лапок; HTML по 3 рядки, 3 сторінки):

```text
> java -cp target/classes ua.duikt.lavrishko.reports.app.Main
Конфігурація 2: CSV + дані з файлу
Авто,Клієнт,Днів,Сума (грн)
Skoda Octavia,Іваненко О.,3,4500
Toyota Camry,"Петренко, І.",5,9000
…
Kia Sportage,"Мельник ""VIP""",7,12600
…
Конфігурація 5: HTML зі сторінками по 3 рядки + дані з файлу
…
<!-- Сторінка 1 з 3 -->
…
<!-- Сторінка 3 з 3 -->
```

## Нова реалізація без зміни сервісу

Далі я додав `MarkdownFormat extends AbstractReportFormat` і конфігурацію 6 у `Main`, а стан етапу 1 перед цим зберіг у `snapshots/stage1_three_formats` (повний вивід порівнянь у `results/evidence.txt`):

```text
> diff -rq snapshots/stage1_three_formats src/main/java
Files snapshots/…/app/Main.java and src/main/java/…/app/Main.java differ
Only in src/main/java/ua/duikt/lavrishko/reports/format: MarkdownFormat.java

> sha256sum snapshots/…/service/ReportService.java src/main/java/…/service/ReportService.java
967146dad637abb8a7588f69bd8ad948e53e7634c48e047f3f8703a6f6cb1645  snapshots/…/service/ReportService.java
967146dad637abb8a7588f69bd8ad948e53e7634c48e047f3f8703a6f6cb1645  src/main/java/…/service/ReportService.java

> git diff --no-index --stat snapshots/…/app/Main.java src/main/java/…/app/Main.java
 .../main/java}/ua/duikt/lavrishko/reports/app/Main.java              | 5 +++++
 1 file changed, 5 insertions(+)
```

Вивід нової реалізації (конфігурація 6, початок таблиці):

```text
Конфігурація 6 (нова): Markdown + дані з файлу
### Оренди за тиждень

| Авто | Клієнт | Днів | Сума (грн) |
| --- | --- | --- | --- |
| Skoda Octavia | Іваненко О. | 3 | 4500 |
| Toyota Camry | Петренко, І. | 5 | 9000 |
…
```

`ReportService.java` змінено на 0 рядків: змінилися лише `Main` (точка збирання, +5 рядків) і з'явився новий файл.

# Висновки

Сервіс генерації звітів залежить лише від інтерфейсів `ReportFormat` і `DataSource`, а всі конкретні об'єкти створюються й передаються через конструктор у `Main`. Спільний порядок побудови звіту винесено в абстрактний клас `AbstractReportFormat`, що прибрало дублювання між чотирма форматами. Додаткову можливість розбиття на сторінки відокремлено в інтерфейс `PagedFormat`: `CsvFormat` не містить зайвих методів, а передачу такого формату в `PagedReportService` відхиляє компілятор. Коли я додав `MarkdownFormat`, у `ReportService` не змінився жоден рядок: `diff -rq` показує лише `Main` і новий файл, `sha256sum` дає той самий хеш. Формат і джерело даних замінюються незалежно одне від одного. Залежності створюються лише в `Main`, тому під час переходу на Spring сервіси не доведеться змінювати.

# Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/lr-4>
