---
pagetitle: "Лабораторна робота № 3"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з лабораторної роботи № 3"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Успадкування, поліморфізм, принцип LSP. Проєктування ієрархії з поліморфною обробкою"
variant: "Варіант 2 (Медіаконтент: Фільм, Пісня, Подкаст)"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---

# Мета роботи

Спроєктувати ієрархію класів з поліморфною обробкою об'єктів через базовий тип і застосувати принцип підстановки Лісков (LSP).

# Завдання за варіантом

Мій номер у списку групи 12, тому я виконую варіант 2. Базовий тип **Медіаконтент** (`MediaContent`), різновиди Фільм, Пісня, Подкаст, спільна операція: тривалість і опис. Об'єкти зберігаються в масиві базового типу, обробляються поліморфно, а додавання різновиду не змінює код обробки.

# Теоретичні відомості

LSP: об'єкти похідного класу можна використовувати там, де очікується базовий, без порушення правильності програми. Похідний клас не посилює передумов, не послаблює постумов, зберігає інваріанти й не кидає нових винятків. Ознака порушення: `instanceof` і приведення типів у клієнтському коді.

# Хід виконання роботи

![Рисунок 1. Діаграма класів](diagrams/classes.png){width=55%}

## Розподіл відповідальності

Базовий абстрактний `MediaContent` містить спільні дані (`title`, `durationSeconds`), перевірку інваріантів, `formattedDuration()` і фінальний `describe()`. Різновиди (`Film`, `Song`, `Podcast`) додають власні поля й перевизначають лише `kind()` та `details()`; базову частину ініціалізують через `super(...)`. Тобто змінна частина спільної операції `describe()` (вид і подробиці) перевизначається в кожному різновиді, а сам каркас `final`, щоб нащадок не порушив формат (див. E1). `LibraryReport` бачить тільки `MediaContent[]`, конкретні класи відомі лише в `Main`.

```java
public abstract class MediaContent {
    private final String title;
    private final int durationSeconds;

    protected MediaContent(String title, int durationSeconds) {
        this.title = requireText(title, "назва");
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("тривалість має бути більшою за 0, отримано " + durationSeconds);
        }
        this.durationSeconds = durationSeconds;
    }
    public abstract String kind();
    protected abstract String details();
    public final String describe() {                    // спільна операція
        return kind() + " «" + title + "», " + formattedDuration() + ". " + details();
    }
}

public class Film extends MediaContent {
    public Film(String title, int durationSeconds, String director, int year) {
        super(title, durationSeconds);                  // базова частина
        this.director = requireText(director, "режисер");
        this.year = year;
    }
    @Override public String kind() { return "Фільм"; }
    @Override protected String details() { return "Режисер: " + director + ", " + year + " р."; }
}
```

`Song` і `Podcast` побудовано так само. Обробка масиву не розрізняє різновиди:

```java
public void printAll(MediaContent[] library) {
    for (MediaContent item : library) { System.out.println(item.describe()); }
}
```

## Поліморфна обробка та додавання різновиду

```text
> java -cp target/classes ua.duikt.lavrishko.media.Main   (етап 1: три різновиди)
…
Фільм «Довга дорога додому», 02:16:00. Режисер: Андрій Мельник, 2019 р.
Пісня «Тихий вечір», 00:03:34. Виконавець: Гурт «Береги», альбом: Світанок.
Подкаст «Код і кава», 00:45:45. Ведучий: Олена Ковальчук, випуск № 12.
Елементів: 3, загальна тривалість: 03:05:19
…
```

Один виклик `describe()` дає різний результат залежно від різновиду. Потім я додав `Audiobook`, а стан етапу 1 зберіг у `snapshots/stage1_three_kinds`:

```text
> diff -rq snapshots/stage1_three_kinds src/main/java
Only in src/main/java/ua/duikt/lavrishko/media: Audiobook.java
Files snapshots/…/media/Main.java and src/main/java/…/media/Main.java differ

> sha256sum snapshots/…/LibraryReport.java src/main/java/…/LibraryReport.java
dd41e3a551f359efbe675e8f3aa7b405bf48bdb1d9002b3dad38338d9c293aad  snapshots/…/LibraryReport.java
dd41e3a551f359efbe675e8f3aa7b405bf48bdb1d9002b3dad38338d9c293aad  src/main/java/…/LibraryReport.java
```

Змінилися лише `Main` (9 доданих рядків, масив із чотирьох елементів) і з'явився `Audiobook.java`; `LibraryReport.java` і `MediaContent.java` ідентичні. Той самий `LibraryReport` обробляє чотири різновиди:

```text
> java -cp target/classes ua.duikt.lavrishko.media.Main   (етап 2: додано Audiobook)
…
Бібліотека з новим різновидом (Audiobook):
…
Аудіокнига «Вечори на хуторі», 05:43:00. Автор: Микола Гоголь, читає: Ігор Савчук.
Елементів: 4, загальна тривалість: 08:48:19
…

> grep -rnE 'instanceof|getClass\(\)|\((Film|Song|Podcast|Audiobook)\) *[a-z]' src/main/java
(код завершення: 1)
```

`grep` по коду етапу 2 не знаходить ні `instanceof`, ні приведень до різновидів (код 1 означає «збігів немає»).

## Захист угоди базового типу

```text
> javac -encoding UTF-8 -cp target/classes compile-errors/E1_override_final/*.java
BadFilm.java:10: error: describe() in BadFilm cannot override describe() in MediaContent
…
  overridden method is final
```

`final` на `describe()` не дає різновиду змінити спільну угоду. Некоректні дані (порожня назва, тривалість 0) відхиляє конструктор до створення об'єкта (`IllegalArgumentException`).

# Обґрунтування підстановки (LSP)

Контракт `MediaContent`: методи без параметрів (передумов до аргументів немає); `describe()` завжди повертає непорожній рядок із видом, назвою й тривалістю; інваріанти (назва не порожня, тривалість більша за 0) перевіряє конструктор, поля `private final`; винятків методи не кидають.

| Різновид | Передумови не посилено | Постумови не послаблено | Інваріанти збережено | Нових винятків немає |
|---|---|---|---|---|
| Фільм | `describe()` і `durationSeconds()` не перевизначено | `kind()`, `details()` непорожні; формат `describe()` `final` | базову частину перевіряє `super(...)`; `director`, `year` `private final` | `kind()`, `details()` нічого не кидають |
| Пісня | так само | так само | так само для `artist`, `album` | так само |
| Подкаст | так само | так само | так само для `host`, `episode` | так само |
| Аудіокнига | так само | так само | так само для `author`, `narrator` | так само |

Код, написаний для `MediaContent`, тому однаково працює з кожним різновидом і бачить різницю лише в тексті опису. У коді обробки немає `instanceof`, а всі чотири різновиди пройшли через один і той самий `LibraryReport`.

# Висновки

Спільні дані (назва, тривалість) і спільну поведінку (форматування, склад опису) винесено в абстрактний базовий клас, а різновиди додають власні поля й реалізують `kind()` та `details()`. Базова частина ініціалізується викликом `super`. Клієнтський код працює з `MediaContent[]` і не містить перевірок типів. Після того як я додав `Audiobook`, `diff -rq` показав лише змінений `Main` і новий файл, а хеш `LibraryReport.java` до і після збігся. Для кожного різновиду перевірено чотири умови LSP, а `final` на `describe()` захищає спільну угоду від зміни в нащадках.

# Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/lr-3>
