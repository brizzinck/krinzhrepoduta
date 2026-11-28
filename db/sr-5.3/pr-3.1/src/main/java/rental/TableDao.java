package rental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** DAO: CRUD для будь-якої таблиці з Table.ALL через чистий JDBC. Значення передаються лише параметрами. */
public class TableDao {
    private final Connection connection;

    public TableDao(Connection connection) {
        this.connection = connection;
    }

    /** SELECT усіх рядків. */
    public int findAll(Table table) throws SQLException {
        String sql = "SELECT * FROM " + table.name() + " ORDER BY " + String.join(", ", table.keys());
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Printer.print(rs);
        }
    }

    /** SELECT одного рядка за ключем. */
    public int findById(Table table, List<Integer> key) throws SQLException {
        String sql = "SELECT * FROM " + table.name() + " WHERE " + table.keyCondition();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindKey(ps, key, 1);
            try (ResultSet rs = ps.executeQuery()) {
                return Printer.print(rs);
            }
        }
    }

    /** INSERT лише заповнених стовпців: для решти діють DEFAULT або NULL. Повертає згенерований ключ або 0. */
    public int insert(Table table, Map<String, String> values) throws SQLException {
        String columns = String.join(", ", values.keySet());
        String marks = String.join(", ", values.keySet().stream().map(c -> "?").toList());
        String sql = "INSERT INTO " + table.name() + " (" + columns + ") VALUES (" + marks + ")";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            for (String value : values.values()) {
                ps.setString(i++, value);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /** UPDATE заданих стовпців. Повертає кількість змінених рядків. */
    public int update(Table table, List<Integer> key, Map<String, String> values) throws SQLException {
        List<String> assignments = new ArrayList<>();
        for (String column : values.keySet()) {
            assignments.add(column + " = ?");
        }
        String sql = "UPDATE " + table.name() + " SET " + String.join(", ", assignments)
            + " WHERE " + table.keyCondition();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            for (String value : values.values()) {
                ps.setString(i++, value);
            }
            bindKey(ps, key, i);
            return ps.executeUpdate();
        }
    }

    /** DELETE за ключем. Повертає кількість видалених рядків. */
    public int delete(Table table, List<Integer> key) throws SQLException {
        String sql = "DELETE FROM " + table.name() + " WHERE " + table.keyCondition();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindKey(ps, key, 1);
            return ps.executeUpdate();
        }
    }

    private static void bindKey(PreparedStatement ps, List<Integer> key, int start) throws SQLException {
        int i = start;
        for (int part : key) {
            ps.setInt(i++, part);
        }
    }
}
