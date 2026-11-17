package ua.duikt.lavrishko.rental.auth;

/** Користувач сервісу: логін, роль і BCrypt-хеш пароля. */
public record User(String login, String role, String passwordHash) {
}
