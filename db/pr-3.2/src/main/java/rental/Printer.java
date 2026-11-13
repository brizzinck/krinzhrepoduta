package rental;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Друкує список записів таблицею. Заголовки це імена компонентів запису, значення беруться з їхніх методів. */
public final class Printer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Printer() {
    }

    public static <T extends Record> void print(List<T> rows, Class<T> type) {
        RecordComponent[] parts = type.getRecordComponents();
        int[] widths = new int[parts.length];
        List<String[]> cells = new ArrayList<>();
        String[] header = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            header[i] = parts[i].getName();
            widths[i] = header[i].length();
        }
        try {
            for (T row : rows) {
                String[] line = new String[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    line[i] = format(parts[i].getAccessor().invoke(row));
                    widths[i] = Math.max(widths[i], line[i].length());
                }
                cells.add(line);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        printRow(header, widths);
        cells.forEach(line -> printRow(line, widths));
        System.out.println("Рядків: " + rows.size());
    }

    private static String format(Object value) {
        if (value == null) {
            return "NULL";
        }
        return value instanceof LocalDateTime time ? time.format(DATE_TIME) : value.toString();
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
