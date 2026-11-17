package ua.duikt.lavrishko.rental.auth;

import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Користувачі в памʼяті (БД для цієї роботи не потрібна). Паролі зберігаються лише як BCrypt-хеші. */
@Component
public class UserStore {
    // Хеші згенеровано один раз; демонстраційні паролі наведено в звіті
    private static final String HASH_OLENA = "$2a$10$ytC0/M9/5svn3QNCj.AXReWhG7DXEPBhQdKAzay0L1NEKGRTE417e";
    private static final String HASH_MANAGER = "$2a$10$hNdcW.3qoENZkChRt073v.bCI7rCrISqijc9Wb3X0dTbv/wkyhDaS";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final Map<String, User> users = Map.of(
            "olena_k", new User("olena_k", "CLIENT", HASH_OLENA),
            "manager", new User("manager", "MANAGER", HASH_MANAGER));

    /** Повертає користувача, якщо пара логін і пароль правильна. */
    public Optional<User> authenticate(String login, String password) {
        User user = users.get(login);
        // Для невідомого логіна теж рахуємо BCrypt, щоб час відповіді не видавав, чи існує користувач
        String hash = user != null ? user.passwordHash() : HASH_MANAGER;
        boolean ok = encoder.matches(password, hash);
        return ok && user != null ? Optional.of(user) : Optional.empty();
    }
}
