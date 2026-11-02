---
pagetitle: "Звіт з практичної роботи № 5"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з практичної роботи № 5"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Винятки, захисне програмування та основи вводу-виводу. Перевірювані та неперевірювані винятки, власні винятки, робота з файлами"
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

Освоїти різницю між перевірюваними й неперевірюваними винятками, створити власний виняток зі збереженням причини та перевірити вплив закриття потоку й кодування на запис і читання кирилиці.

# 2. Завдання за варіантом

Номер у списку групи 12, варіант 2: дані «залишки товарів», власний виняток `InvalidQuantityException`. Виконано три досліди: (1) перевірювані й неперевірювані винятки, порядок `catch`, `finally`; (2) перекидання власного винятку без причини й з причиною; (3) запис через `BufferedWriter` без `close()` і в `try-with-resources`, читання в різних кодуваннях. Maven, Java 21, запуск у docker `maven:3.9-eclipse-temurin-21`; хибні фрагменти лежать у `compile-errors/`, справжні виводи в `results/`.

# 3. Дослід 1. Перевірювані й неперевірювані винятки

Без обробки. Конструктор `FileReader(String)` оголошує `throws FileNotFoundException`; виклик у `main` без `try-catch` і `throws` не компілюється:

```
> javac … compile-errors/E1_unreported/*.java
Unreported.java:6: error: unreported exception FileNotFoundException; must be caught or declared to be thrown
        new FileReader("data.txt");
        ^
```

`FileNotFoundException` перевірюваний (наслідує `IOException`), тож компілятор вимагає рішення: перехопити або оголосити в `throws`.

try-catch і throws. Перший метод обробляє виняток сам, другий передає його викликачу:

```java
    public static void readFirstLineWithCatch(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            System.out.println("Перший рядок: " + reader.readLine());
        } catch (FileNotFoundException e) {
            System.out.println("Файл не знайдено: " + e.getMessage());
        } // … далі catch (IOException e) для решти помилок вводу-виводу
    }

    public static void openWithThrows(String path) throws IOException {
        try (FileReader reader = new FileReader(path)) {
            System.out.println("Файл відкрито: " + path);
        }
    }
```

```
> java … Main exp1 catch nofile.txt
Файл не знайдено: nofile.txt (No such file or directory)
(код завершення: 0)
> java … Main exp1 throws nofile.txt
Exception in thread "main" java.io.FileNotFoundException: nofile.txt (No such file or directory)
	…
	at ua.duikt.lavrishko.pr5.Exp1Checked.openWithThrows(Exp1Checked.java:27)
(код завершення: 1)
```

З `try-catch` програма продовжує роботу (код 0), з `throws` виняток дійшов до `main` і завершив її (код 1). Виправлені варіанти компілюються (`results/fixed.txt`).

Неперевірювані. `total / divisor` і `values[index]` компілюються без `throws` і `try-catch` (`results/compile_ok.txt`), а помилка проявляється лише під час виконання:

```
> java … Main exp1 div 0
Exception in thread "main" java.lang.ArithmeticException: / by zero
	at ua.duikt.lavrishko.pr5.Exp1Checked.divide(Exp1Checked.java:34)
(код завершення: 1)
> java … Main exp1 array 5
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 3
(код завершення: 1)
```

Це підкласи `RuntimeException`: помилка програміста (нуль, поганий індекс), яку виправляють у коді, а не обробляють.

Порядок `catch`. `catch (Exception e)` перед `catch (IOException e)` не компілюється:

```
> javac … compile-errors/E2_catch_order/*.java
WrongOrder.java:11: error: exception IOException has already been caught
        } catch (IOException e) {
          ^
```

Другий блок недосяжний, бо `IOException` уже є `Exception`; порядок має йти від конкретного до загального.

`finally`. Блок виконується і при успіху (`data/stock.txt`), і при помилці (`nofile.txt`):

```
> java … Main exp1 finally data/stock.txt
Файл відкрито й закрито без помилок
Блок finally виконано
> java … Main exp1 finally nofile.txt
Спіймано FileNotFoundException: nofile.txt (No such file or directory)
Блок finally виконано
```

# 4. Дослід 2. Власний виняток і причина помилки

`InvalidQuantityException extends Exception` (перевірюваний, бо некоректні дані очікувані; тип методичка не задає) має два конструктори: `(String message)` і `(String message, Throwable cause)`. Метод розбирає кількість через `Integer.parseInt` і перекидає виняток двома способами:

```java
    public static int parseQuantityWithoutCause(String text) throws InvalidQuantityException {
        try {
            return checkPositive(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException(e.getMessage());          // спосіб 1
        }
    }
    // спосіб 2: у catch ... throw new InvalidQuantityException("Некоректна кількість", e);
```

```
> java … Main exp2 no-cause abc
ua.duikt.lavrishko.pr5.InvalidQuantityException: For input string: "abc"
	at ua.duikt.lavrishko.pr5.Exp2Cause.parseQuantityWithoutCause(Exp2Cause.java:14)
getCause():   null
> java … Main exp2 with-cause abc
ua.duikt.lavrishko.pr5.InvalidQuantityException: Некоректна кількість
	at ua.duikt.lavrishko.pr5.Exp2Cause.parseQuantityWithCause(Exp2Cause.java:23)
Caused by: java.lang.NumberFormatException: For input string: "abc"
	…
	at java.base/java.lang.Integer.parseInt(Integer.java:662)
getCause():   java.lang.NumberFormatException: For input string: "abc"
```

У першому способі губиться клас і стек початкового винятку: лишається тільки скопійований текст, немає рядка `Caused by`, `getCause()` повертає `null`. У другому видно, що причиною був `NumberFormatException` і де саме він виник у `Integer.parseInt`, тоді як власне повідомлення описує ситуацію мовою предметної області.

# 5. Дослід 3. Запис і читання файлів

Три рядки з кирилицею записано через `BufferedWriter` двома способами: без `close()` (`writeWithoutClose`) і в `try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) { … }`.

```
> ls -l files/no_close.txt && cat files/no_close.txt
-rw-r--r-- 1 skalse skalse 0 Nov  2 22:31 files/no_close.txt
> ls -l files/with_close.txt && cat files/with_close.txt
-rw-r--r-- 1 skalse skalse 135 Nov  2 22:31 files/with_close.txt
Привіт зі складу
Артикул A-101: молоко, залишок 15
Артикул A-102: хліб, залишок 40
```

Без `close()` файл порожній (0 байтів, без помилки): 83 символи лишились у буфері на 8192 символи, який скидається лише за `flush()`/`close()`. `try-with-resources` викликає `close()` автоматично навіть за винятку. 135 байтів проти 83 символів: кирилиця в UTF-8 займає по 2 байти.

Читання файлу з `with_close.txt` через `BufferedReader(new InputStreamReader(new FileInputStream(…), charset))`:

```
> java … Main exp3 read files/with_close.txt UTF-8
1: Привіт зі складу
> java … Main exp3 read files/with_close.txt windows-1251
1: РџСЂРёРІС–С‚ Р·С– СЃРєР»Р°РґСѓ
```

UTF-8 збігається з кодуванням запису, тож текст коректний. Кожна кирилична літера займає в UTF-8 два байти, а windows-1251 читає кожен байт як окремий символ, тому з кожної літери виходить два символи (`П` це байти `D0 9F`, які windows-1251 читає як «Рџ»). Помилки при цьому немає: програма не знає, що кодування хибне.

# 6. Висновки

Перевірюваний виняток змушує компілятор вимагати `try-catch` або `throws`, а без цього код не збирається. Неперевірювані `ArithmeticException` і `ArrayIndexOutOfBoundsException` компілюються без вимог і падають під час виконання. Порядок `catch` має йти від конкретного до загального, інакше компілятор відхиляє недосяжний блок, а `finally` виконується і при успіху, і при помилці. Передача в `InvalidQuantityException` лише `e.getMessage()` втрачає клас і стек початкової помилки, а передача `e` другим аргументом зберігає ланцюжок `Caused by`. `BufferedWriter` без `close()` лишив файл порожнім, а `try-with-resources` записав усі рядки. Кирилиця читається правильно лише тоді, коли кодування читання збігається з кодуванням запису: UTF-8 дав «Привіт», а windows-1251 «РџСЂРёРІС–С‚». Тому кодування слід завжди вказувати явно.

# 7. Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/pr-5>
