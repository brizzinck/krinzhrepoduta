package ua.duikt.lavrishko.pr4.exp3.naive;

/** Той самий сервіс після переходу на SMS: довелося змінити його код. */
public class NotificationService {

    private final SmsNotifier notifier = new SmsNotifier();

    public void notifyUser(String user) {
        notifier.send("Вітаємо, " + user + "! Документ готовий.");
    }
}
