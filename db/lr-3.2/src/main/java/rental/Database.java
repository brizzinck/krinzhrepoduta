package rental;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/** Підключення до БД car_rental. Параметри беруться зі змінних середовища, у коді паролів немає. */
public final class Database {
    private Database() {
    }

    /** Jdbi сам відкриває й закриває з'єднання для кожного виклику; плагін дає інтерфейси з анотаціями. */
    public static Jdbi connect() {
        Jdbi jdbi = Jdbi.create(env("DB_URL"), env("DB_USER"), env("DB_PASSWORD"));
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задано змінну середовища " + name);
        }
        return value;
    }
}
