package ua.duikt.lavrishko.pr4.exp3.before;

/** Сервіс сам створює залежність: прив'язаний до EmailNotifier. */
public class NotificationService {

    private final EmailNotifier notifier = new EmailNotifier();

    public void notifyUser(String user) {
        notifier.send("Вітаємо, " + user + "! Документ готовий.");
    }
}
