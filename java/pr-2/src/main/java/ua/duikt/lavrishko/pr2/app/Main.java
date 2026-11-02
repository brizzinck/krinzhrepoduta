package ua.duikt.lavrishko.pr2.app;

import ua.duikt.lavrishko.pr2.model.Account;
import ua.duikt.lavrishko.pr2.model.AccessProbe;
import ua.duikt.lavrishko.pr2.model.AccountNoThis;

/** Запускає п'ять дослідів на класі Account (варіант 2). Main лежить у пакеті app, Account у model. */
public class Main {

    public static void main(String[] args) {
        experiment1Access();
        experiment2Constructors();
        experiment3This();
        experiment4Static();
        experiment5Final();
    }

    private static void experiment1Access() {
        System.out.println("Дослід 1: доступ");
        Account account = new Account("Іваненко І. І.", "UA-100001", 1500.50);
        System.out.println("З іншого класу пакета model (AccessProbe):");
        AccessProbe.probe(account);
        System.out.println("З Main у пакеті app:");
        System.out.println("owner (public): " + account.owner);
        // account.pin, account.balance, account.number з Main не компілюються (compile-errors/E2_access_app)
        System.out.println("number через public-геттер: " + account.getNumber());
        System.out.println("balance через public-геттер: " + account.getBalance());
        System.out.println("pin через метод класу: checkPin(\"0000\") = " + account.checkPin("0000"));
        System.out.println("Підклас в іншому пакеті:");
        new ProbeSub().show();
    }

    private static void experiment2Constructors() {
        System.out.println("Дослід 2: три конструктори");
        Account byDefault = new Account();
        Account full = new Account("Петренко П. П.", "UA-200002", 320.0);
        Account viaThis = new Account("Сидоренко С. С.", "UA-300003");
        System.out.println("без параметрів:       " + byDefault);
        System.out.println("з параметрами:        " + full);
        System.out.println("через this(...):      " + viaThis);
    }

    private static void experiment3This() {
        System.out.println("Дослід 3: this");
        Account withThis = new Account("Коваленко К. К.", "UA-400004", 99.9);
        AccountNoThis withoutThis = new AccountNoThis("Коваленко К. К.", "UA-400004", 99.9);
        System.out.println("з this:   " + withThis);
        System.out.println("без this: " + withoutThis);
    }

    private static void experiment4Static() {
        System.out.println("Дослід 4: static");
        int before = Account.count;
        Account a = new Account("Власник А", "UA-500001");
        Account b = new Account("Власник Б", "UA-500002");
        Account c = new Account("Власник В", "UA-500003");
        System.out.println("лічильник до створення трьох об'єктів: " + before);
        System.out.println("Account.getCount() після створення:     " + Account.getCount());
        System.out.println("Account.count (спільне для всіх):       " + Account.count);
        System.out.println("owner (своє в кожного): " + a.owner + ", " + b.owner + ", " + c.owner);
        System.out.println("id (своє в кожного):    " + a.getId() + ", " + b.getId() + ", " + c.getId());
    }

    private static void experiment5Final() {
        System.out.println("Дослід 5: final");
        Account account = new Account("Фінальний Ф. Ф.", "UA-600001");
        System.out.println("final-поле id: " + account.getId());
        System.out.println("final-змінна limit у методі, dailyLimit(): " + account.dailyLimit());
        // перезапис id і limit не компілюється (compile-errors/E3_final_field, E4_final_local)
    }
}
