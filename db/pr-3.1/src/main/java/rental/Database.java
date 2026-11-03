package rental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Підключення до БД car_rental. Параметри беруться зі змінних середовища, у коді паролів немає. */
public final class Database {
    private Database() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(env("DB_URL"), env("DB_USER"), env("DB_PASSWORD"));
    }

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задано змінну середовища " + name);
        }
        return value;
    }
}
