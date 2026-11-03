import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** ЛР 3.1. Найпростіше підключення: читання через ResultSet, запис через PreparedStatement, try-catch. */
public class ConnectDemo {
    public static void main(String[] args) {
        String url = System.getenv("DB_URL");
        try (Connection connection = DriverManager.getConnection(url, System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))) {
            System.out.println("З'єднання відкрито: " + connection.getCatalog());

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO extras (name, price_per_day) VALUES (?, ?)")) {
                insert.setString(1, "Демо-послуга");
                insert.setBigDecimal(2, new java.math.BigDecimal("15.00"));
                System.out.println("Додано рядків: " + insert.executeUpdate());
            }

            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT extra_id, name, price_per_day FROM extras ORDER BY extra_id");
                 ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt("extra_id") + " | " + rs.getString("name") + " | " + rs.getBigDecimal("price_per_day"));
                }
            }

            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM extras WHERE name = ?")) {
                delete.setString(1, "Демо-послуга");
                System.out.println("Видалено рядків: " + delete.executeUpdate());
            }
        } catch (SQLException e) {
            System.out.println("Помилка БД " + e.getErrorCode() + ": " + e.getMessage());
        }
    }
}
