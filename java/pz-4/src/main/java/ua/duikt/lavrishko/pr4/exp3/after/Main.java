package ua.duikt.lavrishko.pr4.exp3.after;

/** Точка збирання: тут обирається реалізація. */
public class Main {
    public static void main(String[] args) {
        new NotificationService(new EmailNotifier()).notifyUser("Олена");
        new NotificationService(new SmsNotifier()).notifyUser("Олена");
    }
}
