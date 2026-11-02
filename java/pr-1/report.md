---
pagetitle: "Звіт з практичної роботи № 1"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з практичної роботи № 1"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "Платформа та екосистема Java. Створення та збірка Java-проєкту засобами Maven"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---


# 1. Мета роботи та завдання

Набути навичок створення Maven-проєкту на JDK 21, підключення зовнішньої залежності, збірки й запуску артефакту та базового аналізу байт-коду. Варіантів немає. Завдання: Maven-проєкт на JDK 21 із залежністю Apache Commons Lang; `Main` виводить версію JVM, ОС, результат методу із залежності й рядок українською; збірка `mvn clean package`, запуск артефакту; пояснення 2-3 інструкцій байт-коду з `javap -c`. Критерії: збірка без помилок, коректні дані JVM і ОС, кирилиця без «крякозябрів», байт-код пояснено.

# 2. Середовище, код і рішення

Рішення. Збірка й запуск виконані в docker-образі `maven:3.9-eclipse-temurin-21` (JDK 21.0.12.1, Maven 3.9.16), а не в IntelliJ IDEA: нічого не встановлювалось у систему, а `mvn clean package` однаковий в IDE й у терміналі. `JAVA_HOME` і PATH для Windows не налаштовувались. Запуск іде з `LANG=C.UTF-8`, щоб кодування виводу не залежало від образу.

```text
> java -version
openjdk version "21.0.12.1" 2026-08-18 LTS
> javac -version
javac 21.0.12.1
```

Код. Окремий метод `greet()` виділено, щоб його байт-код був коротким:

```java
public class Main {

    public static void main(String[] args) {
        System.out.println("JVM: " + Runtime.version() + " (" + System.getProperty("java.vendor") + ")");
        System.out.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        // Метод із підключеної залежності commons-lang3
        System.out.println("Reversed (Commons Lang): " + StringUtils.reverse("Java"));
        greet();
    }

    /** Окремий метод: його байт-код аналізуємо через javap -c. */
    public static void greet() {
        System.out.println("Привіт зі збірки!");
    }
}
```

`pom.xml` (`ua.duikt.lavrishko:env-check:1.0`): `maven.compiler.release` 21, `project.build.sourceEncoding` UTF-8, залежність `commons-lang3` 3.17.0 (приклад методички). Плагін `maven-dependency-plugin` (`copy-dependencies` у фазі `package`) кладе залежності в `target/lib`, а `maven-jar-plugin` пише в маніфест `Main-Class` і `Class-Path`:

```xml
<manifest>
    <addClasspath>true</addClasspath>
    <classpathPrefix>lib/</classpathPrefix>
    <mainClass>ua.duikt.lavrishko.env.Main</mainClass>
</manifest>
```

# 3. Збірка й запуск

```text
> mvn clean package
[INFO] --- compiler:3.15.0:compile (default-compile) @ env-check ---
[INFO] Compiling 1 source file with javac [debug release 21] to target/classes
…
[INFO] Building jar: /w/target/env-check-1.0.jar
[INFO] BUILD SUCCESS

> unzip -p target/env-check-1.0.jar META-INF/MANIFEST.MF
Class-Path: lib/commons-lang3-3.17.0.jar
Main-Class: ua.duikt.lavrishko.env.Main

> java -jar target/env-check-1.0.jar
JVM: 21.0.12.1+1-LTS (Eclipse Adoptium)
OS: Linux (amd64)
Reversed (Commons Lang): avaJ
Привіт зі збірки!
```

Збірка пройшла без помилок, вивід містить версію JVM, ОС (Linux, бо запуск у контейнері), результат методу з залежності (`avaJ`) і кирилицю без спотворень.

# 4. Аналіз байт-коду

`javap -c -p -cp target/classes ua.duikt.lavrishko.env.Main`, метод `greet()`:

```text
  public static void greet();
    Code:
       0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #60                 // String Привіт зі збірки!
       5: invokevirtual #35                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

- `getstatic #7` кладе на стек статичне поле `System.out` (`#7` це індекс запису в пулі констант).
- `ldc #60` («load constant») кладе на стек рядкову константу з пулу: `Привіт зі збірки!`.
- `invokevirtual #35` бере зі стека рядок і об'єкт `PrintStream` та викликає `println(String)`; реалізація обирається за фактичним типом об'єкта під час виконання.
- `return` завершує метод, що повертає `void`.

Запис `#60` у пулі (`javap -v`) є `String`, що вказує на `Utf8` із текстом: у `.class` рядки зберігаються в модифікованому UTF-8, у пам'яті JVM вони стають UTF-16.

# 5. Журнал інцидентів

Інцидент 1: `no main manifest attribute`. Перший запуск звичайного JAR:

```text
> java -jar target/env-check-1.0.jar
no main manifest attribute, in target/env-check-1.0.jar
(код завершення: 1)
```

`maven-jar-plugin` за замовчуванням не пише `Main-Class` у маніфест. Виправлено `<mainClass>` у налаштуванні плагіна.

Інцидент 2: `NoClassDefFoundError`. Після виправлення JAR запустився, але впав на виклику залежності:

```text
> java -jar target/env-check-1.0.jar
JVM: 21.0.12.1+1-LTS (Eclipse Adoptium)
OS: Linux (amd64)
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils
Caused by: java.lang.ClassNotFoundException: org.apache.commons.lang3.StringUtils
(код завершення: 1)
```

JAR містить лише власні класи, а під час запуску залежності немає в classpath. Виправлено `copy-dependencies` у `target/lib` і `Class-Path: lib/…` у маніфесті.

Інцидент 3: кирилиця як `?` (відтворено навмисно). Сама проблема не виникла, бо образ має UTF-8. Для показу той самий JAR запущено з локаллю `POSIX`:

```text
> LANG=POSIX java -jar target/env-check-1.0.jar
…
?????? ?? ??????!
> java -jar target/env-check-1.0.jar   (LANG=C.UTF-8)
…
Привіт зі збірки!
```

Кодування стандартного виводу JVM бере з локалі терміналу, для `POSIX` це ASCII. Виправлення: `LANG=C.UTF-8` або на Windows `chcp 65001` і `-Dstdout.encoding=UTF-8`.

Інцидент 4: версія JDK. Розбіжностей немає: `java`/`javac` 21.0.12.1 і `release 21`.

# 6. Висновки

Створено Maven-проєкт `env-check` на JDK 21 із залежністю `commons-lang3`, зібрано його командою `mvn clean package` і запущено артефакт. Програма виводить версію JVM, ОС, результат `StringUtils.reverse` і український рядок без спотворень. Під час роботи стало видно, що звичайний JAR не містить залежностей і маніфесту з `Main-Class`, тому потрібні плагіни Maven. Через `javap -c` пояснено інструкції `getstatic`, `ldc`, `invokevirtual` і `return` методу `greet()`. З'ясовано, що кодування стандартного виводу залежить від локалі терміналу, а не від файлів. Усе виконано в docker замість IntelliJ IDEA, результат збірки від цього не залежить.

# 7. Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/pr-1>
