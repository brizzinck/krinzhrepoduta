package ua.duikt.lavrishko.pr8;

/** Друга залежність: повідомлення в чат. Реалізації немає, у тестах підставляється мок. */
public interface ChatNotifier {
    void send(String login, String text);
}
