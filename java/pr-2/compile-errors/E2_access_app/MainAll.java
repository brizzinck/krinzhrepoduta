package ua.duikt.lavrishko.pr2.app;

import ua.duikt.lavrishko.pr2.model.Account;

/** Клас Main із пакета app звертається до всіх чотирьох членів Account. */
public class MainAll {
    public static void main(String[] args) {
        Account account = new Account();
        String a = account.pin;       // private
        double b = account.balance;   // за замовчуванням
        String c = account.number;    // protected
        String d = account.owner;     // public
    }
}
