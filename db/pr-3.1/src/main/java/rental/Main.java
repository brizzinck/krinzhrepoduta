package rental;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Консольне меню додатку «Прокат автомобілів». */
public class Main {
    private static final Scanner IN = new Scanner(System.in, StandardCharsets.UTF_8);
    // якщо ввід іде з файлу, друкуємо його, щоб вивід читався як сеанс роботи
    private static final boolean ECHO = System.console() == null;

    public static void main(String[] args) {
        try (Connection connection = Database.connect()) {
            System.out.println("Підключено до БД: " + connection.getCatalog());
            TableDao tables = new TableDao(connection);
            QueryDao queries = new QueryDao(connection);
            printMenu();
            boolean running = true;
            while (running) {
                System.out.println();
                String choice = ask("Ваш вибір");
                // кожна операція в окремому try: помилка БД не завершує програму
                try {
                    running = handle(choice, tables, queries);
                } catch (SQLException e) {
                    System.out.println(describe(e));
                } catch (NumberFormatException e) {
                    System.out.println("Помилка вводу: очікується число");
                }
            }
        } catch (SQLException e) {
            System.out.println("Не вдалося підключитися до БД: " + e.getMessage());
        }
    }

    private static boolean handle(String choice, TableDao tables, QueryDao queries) throws SQLException {
        switch (choice) {
            case "1" -> tables.findAll(askTable());
            case "2" -> {
                Table table = askTable();
                tables.findById(table, askKey(table));
            }
            case "3" -> create(tables);
            case "4" -> update(tables);
            case "5" -> {
                Table table = askTable();
                System.out.println("Видалено рядків: " + tables.delete(table, askKey(table)));
            }
            case "6" -> queries.activeRentals();
            case "7" -> queries.freeCars(ask("Місто"), new BigDecimal(ask("Максимальна ціна за добу")));
            case "8" -> queries.branchRevenue();
            case "9" -> printMenu();
            case "0" -> {
                System.out.println("Роботу завершено");
                return false;
            }
            default -> System.out.println("Немає такого пункту меню");
        }
        return true;
    }

    private static void create(TableDao tables) throws SQLException {
        Table table = askTable();
        Map<String, String> values = new LinkedHashMap<>();
        if (!table.autoKey()) {
            for (String column : table.keys()) {
                values.put(column, ask(column));
            }
        }
        System.out.println("Порожнє значення: DEFAULT або NULL");
        askColumns(table, values);
        int key = tables.insert(table, values);
        System.out.println(key > 0 ? "Додано рядок, ключ: " + key : "Додано рядок");
    }

    private static void update(TableDao tables) throws SQLException {
        Table table = askTable();
        List<Integer> key = askKey(table);
        Map<String, String> values = new LinkedHashMap<>();
        System.out.println("Порожнє значення: стовпець не змінюється");
        askColumns(table, values);
        if (values.isEmpty()) {
            System.out.println("Змін немає");
            return;
        }
        System.out.println("Змінено рядків: " + tables.update(table, key, values));
    }

    private static void askColumns(Table table, Map<String, String> values) {
        for (String column : table.columns()) {
            String value = ask(column);
            if (!value.isEmpty()) {
                values.put(column, value);
            }
        }
    }

    private static List<Integer> askKey(Table table) {
        List<Integer> key = new ArrayList<>();
        for (String column : table.keys()) {
            key.add(Integer.parseInt(ask(column)));
        }
        return key;
    }

    private static Table askTable() {
        for (int i = 0; i < Table.ALL.size(); i++) {
            System.out.print((i + 1) + "." + Table.ALL.get(i).name() + "  ");
        }
        System.out.println();
        int number = Integer.parseInt(ask("Таблиця"));
        if (number < 1 || number > Table.ALL.size()) {
            throw new NumberFormatException();
        }
        return Table.ALL.get(number - 1);
    }

    private static String ask(String prompt) {
        System.out.print(prompt + ": ");
        if (!IN.hasNextLine()) {
            return "0";
        }
        String line = IN.nextLine().trim();
        if (ECHO) {
            System.out.println(line);
        }
        return line;
    }

    private static void printMenu() {
        System.out.println("1 Показати таблицю   2 Знайти за ключем   3 Додати   4 Змінити   5 Видалити");
        System.out.println("6 Активні оренди (JOIN)   7 Вільні авто (фільтр)   8 Виручка філій (агрегація)");
        System.out.println("9 Меню   0 Вихід");
    }

    /** Зрозуміле повідомлення за кодом помилки MySQL. */
    private static String describe(SQLException e) {
        String reason = switch (e.getErrorCode()) {
            case 1062 -> "таке значення вже є (UNIQUE)";
            case 1451 -> "на рядок посилаються інші таблиці (FOREIGN KEY)";
            case 1452 -> "немає рядка, на який іде посилання (FOREIGN KEY)";
            case 3819 -> "порушено обмеження CHECK";
            case 1048, 1364 -> "не заповнено обов'язковий стовпець (NOT NULL)";
            default -> "помилка БД";
        };
        return "Помилка " + e.getErrorCode() + ": " + reason + ". " + e.getMessage();
    }
}
