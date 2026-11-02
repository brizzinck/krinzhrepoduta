package ua.duikt.lavrishko.pr2.model;

/**
 * Інший клас того самого пакета model: пробує звернутися до чотирьох членів Account.
 * Працюючий варіант: звернення до private-поля прибрано (воно не компілюється,
 * див. compile-errors/E1_access_model).
 */
public final class AccessProbe {

    private AccessProbe() {
    }

    public static void probe(Account account) {
        // private pin: НЕ компілюється з іншого класу, навіть того самого пакета
        System.out.println("balance (за замовчуванням): " + account.balance);
        System.out.println("number  (protected):        " + account.number);
        System.out.println("owner   (public):           " + account.owner);
    }
}
