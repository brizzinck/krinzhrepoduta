package rental;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Друкує ResultSet у консоль таблицею. Назви стовпців беруться з метаданих. */
public final class Printer {
    private Printer() {
    }

    public static int print(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        int[] widths = new int[count];
        List<String[]> rows = new ArrayList<>();
        String[] header = new String[count];
        for (int i = 0; i < count; i++) {
            header[i] = meta.getColumnLabel(i + 1);
            widths[i] = header[i].length();
        }
        while (rs.next()) {
            String[] row = new String[count];
            for (int i = 0; i < count; i++) {
                String value = rs.getString(i + 1);
                row[i] = value == null ? "NULL" : value;
                widths[i] = Math.max(widths[i], row[i].length());
            }
            rows.add(row);
        }
        printRow(header, widths);
        for (String[] row : rows) {
            printRow(row, widths);
        }
        System.out.println("Рядків: " + rows.size());
        return rows.size();
    }

    private static void printRow(String[] row, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            line.append(String.format("%-" + widths[i] + "s", row[i]));
            if (i < row.length - 1) {
                line.append(" | ");
            }
        }
        System.out.println(line);
    }
}
