package ua.duikt.lavrishko.pr5;

/**
 * Некоректна кількість товару на складі (варіант 2).
 * Перевірюваний виняток (наслідує Exception, а не RuntimeException): помилка в даних очікувана,
 * тому викликальний код зобов'язаний або обробити її, або оголосити в throws.
 */
public class InvalidQuantityException extends Exception {

    /** Лише повідомлення: початкова причина тут не зберігається. */
    public InvalidQuantityException(String message) {
        super(message);
    }

    /** Повідомлення й причина: низькорівневий виняток лишається в ланцюжку (Caused by). */
    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
