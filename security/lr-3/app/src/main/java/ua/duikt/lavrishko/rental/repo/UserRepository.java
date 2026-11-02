package ua.duikt.lavrishko.rental.repo;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.duikt.lavrishko.rental.dto.RegistrationForm;

/** Доступ до таблиці users. Усі запити параметризовані: дані користувача ніколи не стають частиною SQL-тексту. */
@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsByLogin(String login) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE login = ?", Integer.class, login);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    /** Роль не приймається ззовні: завжди CLIENT. */
    public void insert(RegistrationForm form, String passwordHash) {
        jdbc.update(
                "INSERT INTO users (login, first_name, email, password_hash, phone, birth_date, role) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'CLIENT')",
                form.getLogin(), form.getFirstName(), form.getEmail(), passwordHash, form.getPhone(),
                java.sql.Date.valueOf(form.getBirthDate()));
    }

    public List<Map<String, Object>> findPublicByLogin(String login) {
        return jdbc.queryForList("SELECT login, first_name, email, role FROM users WHERE login = ?", login);
    }
}
