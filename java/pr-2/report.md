---
pagetitle: "Звіт з практичної роботи № 2"
university: "Державний університет інформаційно-комунікаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з практичної роботи № 2"
discipline: "Конструювання програмного забезпечення JAVA"
topic: "ООП у Java: класи, інкапсуляція, конструктори, static. Модифікатори доступу, конструктори, this, static та final на практиці"
variant: "Варіант 2 (Рахунок)"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірив:"
teacher: ["Ніщеменко Дмитро Олександрович"]
city-year: "Київ – 2026"
---

<style>
pre, pre code { overflow: visible !important; overflow-wrap: anywhere !important; }
</style>


# 1. Мета роботи

Експериментально перевірити рівні доступу, види конструкторів, `this`, `static` і `final`, зафіксувати повідомлення компілятора та пояснити різницю доступу між двома пакетами.

# 2. Завдання за варіантом

Номер у списку групи 12, варіант 2: сутність **Рахунок** (власник, номер, баланс). `Account` у пакеті `model`, `Main` у пакеті `app`. Maven, Java 21. П'ять дослідів: доступ, три конструктори, `this`, `static`, `final`. Повні хибні фрагменти лежать у `compile-errors/`, виводи в `results/`.

# 3. Дослід 1. Модифікатори доступу

Клас `Account` має чотири поля з різним доступом:

```java
public class Account {
    public static int count;        // дослід 4
    private String pin = "0000";    // private: лише всередині класу Account
    double balance;                 // за замовчуванням (package-private): весь пакет model
    protected String number;        // protected: пакет model і підкласи з будь-якого пакета
    public String owner;            // public: звідусіль
    private final String id;        // дослід 5
```

Звернення до всіх чотирьох полів з іншого класу пакета `model` (`AccessProbeAll`) і з `Main` пакета `app` (`MainAll`): `String a = account.pin; double b = account.balance; String c = account.number; String d = account.owner;`. Повідомлення `javac` тут і в досліді 5 скорочено до рядків з помилками, повні лежать у `results/`.

```
> javac … compile-errors/E1_access_model/*.java
AccessProbeAll.java:7: error: pin has private access in Account
1 error
> javac … compile-errors/E2_access_app/*.java
MainAll.java:9: error: pin has private access in Account
MainAll.java:10: error: balance is not public in Account; cannot be accessed from outside package
MainAll.java:11: error: number has protected access in Account
3 errors
```

З пакета `model` не компілюється лише `private`. З `Main` в іншому пакеті лишається тільки `public`: за замовчуванням доступ обмежено пакетом `model`, а `protected` відкритий пакету й підкласам, `Main` не підклас `Account`. Таблицю 1 я склав за фактичним виводом `javac`, вона збігається з очікуваною.

: Таблиця 1 – Доступ до полів `Account` з двох пакетів

| Рівень доступу (поле) | З іншого класу пакета model | З Main у пакеті app |
|---|---|---|
| `private` (`pin`) | ні | ні |
| за замовчуванням (`balance`) | так | ні |
| `protected` (`number`) | так | ні |
| `public` (`owner`) | так | так |

Робочий код без хибних звернень компілюється (`results/compile_ok.txt`). `Main` читає `number` і `balance` через публічні геттери, а `protected` доступний підкласу `ProbeSub` з пакета `app` (у дослідах 2-5 показано частини того самого виводу):

```
> java -cp target/classes ua.duikt.lavrishko.pr2.app.Main
Дослід 1: доступ
З іншого класу пакета model (AccessProbe):
balance (за замовчуванням): 1500.5
number  (protected):        UA-100001
owner   (public):           Іваненко І. І.
З Main у пакеті app:
owner (public): Іваненко І. І.
number через public-геттер: UA-100001
balance через public-геттер: 1500.5
pin через метод класу: checkPin("0000") = true
Підклас в іншому пакеті:
protected number з підкласу в app: UA-777777
…
```

# 4. Дослід 2. Три конструктори

```java
    public Account() {                                        // типові значення
        this.owner = "Невідомий";
        this.number = "UA-000000";
        this.balance = 0.0;
        this.id = nextId();
    }

    public Account(String owner, String number, double balance) {   // з параметрами
        this.owner = owner;
        this.number = number;
        this.balance = balance;
        this.id = nextId();
    }

    public Account(String owner, String number) {             // через this(...)
        this(owner, number, 0.0);
    }
```

```
Дослід 2: три конструктори
без параметрів:       Account{id=ID-3, owner=Невідомий, number=UA-000000, balance=0.0}
з параметрами:        Account{id=ID-4, owner=Петренко П. П., number=UA-200002, balance=320.0}
через this(...):      Account{id=ID-5, owner=Сидоренко С. С., number=UA-300003, balance=0.0}
```

Перший об'єкт отримав типові значення, другий усі значення з аргументів, третій власника й номер з аргументів, а баланс 0.0 з основного конструктора; `this(…)` не дублює код ініціалізації.

# 5. Дослід 3. Ключове слово this

У конструкторі з параметрами `this.owner = owner` присвоює полю значення однойменного параметра (див. код вище). У класі `AccountNoThis` `this` прибрано (`owner = owner; number = number; balance = balance;`). Він компілюється без помилок, а результат інший:

```
Дослід 3: this
з this:   Account{id=ID-6, owner=Коваленко К. К., number=UA-400004, balance=99.9}
без this: AccountNoThis{owner=null, number=null, balance=0.0}
```

Без `this` ім'я `owner` у конструкторі означає параметр (він затіняє поле), тож параметр присвоюється сам собі, а поля лишаються `null` і `0.0`. Компілятор мовчить, тому такий дефект небезпечний.

# 6. Дослід 4. static

`count` оголошено `static`, а `owner`, `number`, `id` звичайні; статичний метод викликається через ім'я класу:

```java
    private static String nextId() {
        count++;
        return "ID-" + count;
    }

    public static int getCount() {
        return count;
    }
```

```
Дослід 4: static
лічильник до створення трьох об'єктів: 6
Account.getCount() після створення:     9
Account.count (спільне для всіх):       9
owner (своє в кожного): Власник А, Власник Б, Власник В
id (своє в кожного):    ID-7, ID-8, ID-9
```

Статичне поле існує в одному екземплярі й належить класу, тому три нові об'єкти збільшили спільний лічильник з 6 до 9 (6 рахунків створено в дослідах 1-3); `owner` та `id` у кожного свої.

# 7. Дослід 5. final

Поле `id` оголошено `private final` (значення задається в конструкторі), у методі `dailyLimit()` оголошено `final int limit = 5;`, і метод повертає `limit * 1000`. Спроби перезаписати зроблено в окремих фрагментах з таким самим оголошенням: `this.id = newId;` у методі `rename` (`AccountFinalField`) і `limit = 6;` (`FinalLocal`):

```
> javac … compile-errors/E3_final_field/*.java
AccountFinalField.java:10: error: cannot assign a value to final variable id
> javac … compile-errors/E4_final_local/*.java
FinalLocal.java:5: error: cannot assign a value to final variable limit
> java -cp target/classes ua.duikt.lavrishko.pr2.app.Main
…
Дослід 5: final
final-поле id: ID-10
final-змінна limit у методі, dailyLimit(): 5000
```

У конструкторі `this.id = nextId()` допустимо, бо це перше й єдине присвоєння, у методі після ініціалізації воно заборонено; те саме для локальної змінної: перше присвоєння в оголошенні, `limit = 6` уже друге.

# 8. Висновки

Повідомлення `javac` підтвердили правила доступу: `private` недоступний навіть іншому класу того самого пакета, доступ за замовчуванням відкритий пакету `model`, але закритий для `Main` у пакеті `app`, `protected` додатково доступний підкласам, `public` доступний усім. Виклик `this(…)` у третьому конструкторі прибрав дублювання ініціалізації. Без `this` у конструкторі параметр затіняє поле: код компілюється, але поля лишаються `null` і `0.0`. Статичне поле `count` спільне для всіх об'єктів і доступне через ім'я класу, а `owner` та `id` свої в кожного. Повторне присвоєння `final`-полю чи `final`-змінній компілятор відхиляє. Поле `pin` я читаю лише через `checkPin`, тому воно лишається закритим навіть для `Main`.

# 9. Посилання на репозиторій

<https://github.com/brizzinck/krinzhrepoduta/tree/main/java/pr-2>
