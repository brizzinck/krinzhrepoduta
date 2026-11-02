package ua.duikt.lavrishko.pr4.exp3.after;

/** Абстракція, від якої залежить сервіс. */
public interface Notifier {
    void send(String message);
}
