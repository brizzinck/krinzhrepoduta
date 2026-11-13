package ua.duikt.lavrishko.lr6;

/** Учасник команди: логін, ПІБ, кількість закритих задач. */
public record Member(String login, String fullName, int closedCount) implements Keyed<String> {
    @Override
    public String key() {
        return login;
    }

    @Override
    public String toString() {
        return login + " " + fullName + ", закрито " + closedCount;
    }
}
