package ua.duikt.lavrishko.pr2.model;

/** Інший клас того самого пакета model звертається до всіх чотирьох членів Account. */
public class AccessProbeAll {
    public static void main(String[] args) {
        Account account = new Account();
        String a = account.pin;       // private
        double b = account.balance;   // за замовчуванням
        String c = account.number;    // protected
        String d = account.owner;     // public
    }
}
