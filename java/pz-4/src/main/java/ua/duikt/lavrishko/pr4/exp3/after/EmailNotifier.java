package ua.duikt.lavrishko.pr4.exp3.after;

public class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
