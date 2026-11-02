package ua.duikt.lavrishko.pr2.model;

/**
 * Дослід 3: той самий конструктор з параметрами, але без this. Компілюється без помилок,
 * однак параметр "затіняє" поле, тож присвоєння змінює параметр, а поле лишається за замовчуванням.
 */
public class AccountNoThis {

    public String owner;
    public String number;
    public double balance;

    public AccountNoThis(String owner, String number, double balance) {
        owner = owner;
        number = number;
        balance = balance;
    }

    @Override
    public String toString() {
        return "AccountNoThis{owner=" + owner + ", number=" + number + ", balance=" + balance + "}";
    }
}
