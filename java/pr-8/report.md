---
pagetitle: "Звіт з практичної роботи № 8"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з практичної роботи № 8"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Модульне тестування та керування залежностями. JUnit, параметризовані тести та Mockito"
variant: "Варіант 2 (трекер задач)"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---

<style>
pre, pre code { overflow: visible !important; overflow-wrap: anywhere !important; }
</style>

# 1. Мета роботи

Написати модульні тести JUnit за схемою Given-When-Then, перевірити межі параметризованими тестами й ізолювати сервіс від залежностей за допомогою Mockito.

# 2. Завдання за варіантом

Номер у списку групи 12, варіант 2 (12 mod 5): `Sprint.canAdd(points, current)`: оцінка задачі від 1 до 13, разом у спринті не більше 40 SP. Межі: оцінки 0, 1, 13, 14, сума 40 і 41. Сервіс `IssueService`: поточне навантаження виконавця береться з `TeamDirectory`, повідомлення йде в `ChatNotifier`. Maven, Java 21, JUnit 6.1.3 (через `junit-bom`), `mockito-junit-jupiter` 5.24.0, Surefire 3.6.1, запуск у docker. Повні логи кожного кроку в `results/`.

![Клас, сервіс і залежності](img/classes.png){width=76%}

# 3. Клас і правило

Оцінка поза 1..13 дає `IllegalArgumentException`. Від'ємну поточну суму я теж вважаю некоректною й кидаю той самий виняток (методичка про неї не каже). Ємність спринту `CAPACITY = 40`.

```java
    public boolean canAdd(int points, int current) {
        if (points < MIN_POINTS || points > MAX_POINTS) {
            throw new IllegalArgumentException("оцінка задачі " + points + " поза межами "
                    + MIN_POINTS + ".." + MAX_POINTS);
        }
        if (current < 0) {
            throw new IllegalArgumentException("поточна сума " + current + " не може бути від'ємною");
        }
        return current + points <= CAPACITY;
    }
```

# 4. Дослід 1. Три тести Given-When-Then

`SprintTest`: звичайний випадок (30 SP у спринті, задача на 5 SP, разом 35, очікується `true`), оцінка 0 (`assertThrows`) і повний спринт (38 SP і задача на 5 SP, разом 43, очікується `false`).

```java
    @Test
    void addsTaskWhenItFits() {
        // Given: у спринті 30 SP, задача на 5 SP (разом 35 з 40)
        int points = 5;
        int current = 30;
        // When
        boolean result = sprint.canAdd(points, current);
        // Then
        assertTrue(result);
    }
```

```
> mvn test -Dtest=SprintTest                 (results/s1_green.txt)
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Потім я замінив у коді `CAPACITY = 40` на `30` і запустив тести ще раз:

```
> mvn test -Dtest=SprintTest                 (results/s1_red.txt)
[ERROR]   SprintTest.addsTaskWhenItFits:20 expected: <true> but was: <false>
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0
```

Упав лише `addsTaskWhenItFits`: сума 35 з обмеженням 30 вже не вміщується. Інші два тести пройшли, бо оцінка 0 і сума 43 відхиляються і за 30, і за 40. Звичайні значення помітили зміну лише одним тестом, тому межі перевіряє окремий набір (розділ 5). Код повернуто.

# 5. Дослід 2. Параметризовані тести на межах

Допустимі значення (оцінка, поточна сума, результат) задає `@CsvSource`, недопустимі `@ValueSource` з `assertThrows`:

```java
    @ParameterizedTest
    @CsvSource({
            "1,  0,  true",    // найменша допустима оцінка
            "13, 0,  true",    // найбільша допустима оцінка
            "13, 27, true",    // разом рівно 40
            "13, 28, false"    // разом 41
    })
    void boundariesInsideRange(int points, int current, boolean expected) {
        assertEquals(expected, sprint.canAdd(points, current));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 14})
    void pointsOutsideRangeThrow(int points) {
        assertThrows(IllegalArgumentException.class, () -> sprint.canAdd(points, 0));
    }
```

Сьомий тест, `negativeCurrentThrows`, перевіряє від'ємну поточну суму (розділ 3).

Значення стоять на самій межі й по обидва боки від неї: 1 і 13 останні допустимі оцінки, 0 і 14 перші недопустимі, сума 40 остання допустима, 41 перша недопустима. Саме на межах заміна `<=` на `<` дає помилку.

```
> mvn test -Dtest=SprintBoundaryTest         (results/s2_green.txt)
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

Я змінив `current + points <= CAPACITY` на `<` і запустив ще раз:

```
> mvn test -Dtest=SprintBoundaryTest         (results/s2_red.txt)
[ERROR] …SprintBoundaryTest.boundariesInsideRange(int, int, boolean)[3] -- … <<< FAILURE!
[ERROR]   SprintBoundaryTest.boundariesInsideRange:23 expected: <true> but was: <false>
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
```

Упав рядок `[3]`, `"13, 27, true"`, тобто сума рівно 40. Інші рядки пройшли. Код повернуто.

# 6. Дослід 3. Сервіс, стаб і мок

`IssueService.plan(login, points)` бере поточну суму з `TeamDirectory`, питає `Sprint.canAdd` і надсилає повідомлення через `ChatNotifier`. Метод сервісу:

```java
public boolean plan(String login, int points) {
    int current = directory.currentPoints(login);
    boolean added = sprint.canAdd(points, current);
    String text = added
            ? "Задачу на " + points + " SP додано в спринт"
            : "Задача на " + points + " SP не вміщується в спринт (зараз " + current + " SP)";
    notifier.send(login, text);
    return added;
}
```

У тесті (`@ExtendWith(MockitoExtension.class)`) довідник повертає задане число, а повідомлення перевіряє `verify`:

```java
    @Mock
    private TeamDirectory directory;   // стаб: лише повертає дані
    @Mock
    private ChatNotifier notifier;     // мок: перевіряємо виклик
    …
    @Test
    void notifiesWhenTaskIsAdded() {
        when(directory.currentPoints("olena")).thenReturn(30);

        boolean added = service.plan("olena", 5);

        assertTrue(added);
        verify(notifier).send("olena", "Задачу на 5 SP додано в спринт");
    }
```

Стаб `TeamDirectory` лише підставляє вхідні дані, результат видно по `assertTrue`. Мок `ChatNotifier` нічого не повертає, тож результат роботи сервісу видно лише через перевірку виклику `send` з потрібними аргументами. Потім у `plan` я закоментував рядок `notifier.send(login, text);`:

```
> mvn test -Dtest=IssueServiceTest           (results/s3_red.txt)
[ERROR]   IssueServiceTest.notifiesWhenTaskIsAdded:35
Wanted but not invoked:
notifier.send(
    "olena",
    "Задачу на 5 SP додано в спринт"
);
Actually, there were zero interactions with this mock.
[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
```

Mockito показує очікуваний виклик і те, що взаємодій не було. Обидва тести впали, бо в обох є `verify`. Код повернуто.

Попередження про агента. Без `argLine` (`pom-without-agent.xml`) Mockito на Java 21 підключає агента динамічно, і JDK попереджає (`results/s3_noagent.txt`):

```
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. …
WARNING: A Java agent has been loaded dynamically (…/byte-buddy-agent-1.17.7.jar)
WARNING: Dynamic loading of agents will be disallowed by default in a future release
```

У `pom.xml` я додав `maven-dependency-plugin` (ціль `properties`), порожню `<argLine/>` у властивостях і в Surefire `<argLine>@{argLine} -javaagent:${org.mockito:mockito-core:jar}</argLine>`. Тепер агент підключено явно, і ці попередження зникли (`results/s3_green.txt`, `results/final_green.txt`). Інше повідомлення JVM про спільні класи (`Sharing is only supported for boot loader classes…`) є і без `argLine` (`results/s3_noagent.txt`), тож до агента не стосується. Остаточний запуск на відновленому коді:

```
> mvn test                                   (results/final_green.txt)
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

# 7. Відповіді на контрольні питання

**1. Навіщо в `assertEquals` для дробових чисел передають похибку?** Дробові числа в `double` зберігаються наближено (0.1 + 0.2 не дорівнює 0.3), тому точне порівняння може хибно падати. Похибка задає допустиму різницю. У моєму варіанті результати цілі й логічні, тож вона не потрібна.

**2. Чому найважливіші тести на межових значеннях?** Помилки в умовах (`<` замість `<=`) проявляються лише на межі. Це показав дослід 2: тест на сумі 40 упав.

**3. Чим параметризований тест кращий за цикл у одному тесті?** Кожен рядок набору це окремий запуск зі своїм номером: у досліді 2 видно, що впав саме `[3]`. Цикл зупинився б на першій помилці.

**4. Що перевіряє стаб, а що мок? Коли достатньо `assertEquals`, а коли потрібен `verify`?** Стаб підставляє дані й нічого не перевіряє. Мок фіксує виклики, їх перевіряє `verify`. Якщо метод повертає результат (`plan` повертає `boolean`), досить `assertTrue`. Якщо він лише передає дані залежності (`send` нічого не повертає), потрібен `verify`.

# 8. Висновки

`Sprint.canAdd` покрито трьома тестами за схемою Given-When-Then, і при заміні обмеження 40 на 30 упав один тест (`addsTaskWhenItFits`). Параметризований набір (`@CsvSource` і `@ValueSource`) пройшов, а після заміни `<=` на `<` упав рядок `[3]`, сума рівно 40. Сервіс `IssueService` перевірено зі стабом `TeamDirectory` і моком `ChatNotifier`, а прибраний виклик `send` дав `Wanted but not invoked`. Явний `-javaagent` в `argLine` прибрав попередження про динамічний агент, а фінальний `mvn test` дав 12 тестів без помилок.

# 9. Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/pr-8>
