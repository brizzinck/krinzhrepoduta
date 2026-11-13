package northwind;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Друкує рядки результату таблицею. Значення беруться з Object[], який повертає Hibernate. */
public final class Printer {
    private Printer() {
    }

    /** Друкує перші {@code limit} рядків і загальну кількість; limit менший за 1 означає всі рядки. */
    public static void print(String[] header, List<Object[]> rows, int limit) {
        int shown = limit < 1 ? rows.size() : Math.min(limit, rows.size());
        int[] widths = new int[header.length];
        List<String[]> cells = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            widths[i] = header[i].length();
        }
        for (Object[] row : rows.subList(0, shown)) {
            String[] line = new String[header.length];
            for (int i = 0; i < header.length; i++) {
                line[i] = format(row[i]);
                widths[i] = Math.max(widths[i], line[i].length());
            }
            cells.add(line);
        }
        printRow(header, widths);
        cells.forEach(line -> printRow(line, widths));
        System.out.println(shown < rows.size() ? "Показано " + shown + " з " + rows.size() : "Рядків: " + rows.size());
    }

    private static String format(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Timestamp t) {
            return t.toLocalDateTime().toLocalDate().toString();
        }
        if (value instanceof BigDecimal d) {
            return d.toPlainString();
        }
        return value.toString();
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
