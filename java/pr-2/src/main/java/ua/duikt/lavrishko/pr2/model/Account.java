package ua.duikt.lavrishko.pr2.model;

/**
 * Рахунок (варіант 2): власник, номер, баланс.
 * Клас використано для п'яти дослідів: рівні доступу, конструктори, this, static, final.
 */
public class Account {

    // Дослід 4: статичне поле належить класу й спільне для всіх об'єктів
    public static int count;

    // Дослід 1: чотири рівні доступу
    private String pin = "0000";    // private: лише всередині класу Account
    double balance;                 // за замовчуванням (package-private): весь пакет model
    protected String number;        // protected: пакет model і підкласи з будь-якого пакета
    public String owner;            // public: звідусіль

    // Дослід 5: final-поле задається один раз, у конструкторі
    private final String id;

    /** Дослід 2, спосіб 1: конструктор без параметрів із типовими значеннями. */
    public Account() {
        this.owner = "Невідомий";
        this.number = "UA-000000";
        this.balance = 0.0;
        this.id = nextId();
    }

    /** Дослід 2, спосіб 2 і дослід 3: конструктор з параметрами; this розрізняє поле й однойменний параметр. */
    public Account(String owner, String number, double balance) {
        this.owner = owner;
        this.number = number;
        this.balance = balance;
        this.id = nextId();
    }

    /** Дослід 2, спосіб 3: конструктор викликає інший через this(...) і передає баланс за замовчуванням. */
    public Account(String owner, String number) {
        this(owner, number, 0.0);
    }

    /** Дослід 4: лічильник створених рахунків; id призначається один раз у конструкторі. */
    private static String nextId() {
        count++;
        return "ID-" + count;
    }

    /** Дослід 4: статичний метод, викликається через ім'я класу. */
    public static int getCount() {
        return count;
    }

    /** Дослід 5: final-змінна в методі, яку неможливо перезаписати. */
    public int dailyLimit() {
        final int limit = 5;
        return limit * 1000;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public double getBalance() {
        return balance;
    }

    /** Доступ до private-поля є лише через метод самого класу. */
    public boolean checkPin(String candidate) {
        return pin.equals(candidate);
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", owner=" + owner + ", number=" + number + ", balance=" + balance + "}";
    }
}
