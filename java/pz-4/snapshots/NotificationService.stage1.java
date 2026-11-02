package ua.duikt.lavrishko.pr4.exp3.after;

/** Залежність приходить ззовні через конструктор як інтерфейс. */
public class NotificationService {

    private final Notifier notifier;

    public NotificationService(Notifier notifier) {
        this.notifier = notifier;
    }

    public void notifyUser(String user) {
        notifier.send("Вітаємо, " + user + "! Документ готовий.");
    }
}
