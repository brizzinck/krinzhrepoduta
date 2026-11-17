package northwind;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.hibernate.SessionFactory;

/** Консольне меню додатку на Hibernate ORM: перегляд таблиць і запити 3-6 з ПР 1.2. */
public class Main {
    private static final Scanner IN = new Scanner(System.in, StandardCharsets.UTF_8);
    // якщо ввід іде з файлу, друкуємо його, щоб вивід читався як сеанс роботи
    private static final boolean ECHO = System.console() == null;
    // Hibernate пише через java.util.logging; рівень SEVERE прибирає службові повідомлення з виводу меню
    private static final java.util.logging.Logger HIBERNATE_LOG = java.util.logging.Logger.getLogger("org.hibernate");

    static {
        HIBERNATE_LOG.setLevel(java.util.logging.Level.SEVERE);
    }

    public static void main(String[] args) {
        SessionFactory factory;
        try {
            factory = Database.open();
        } catch (RuntimeException e) {
            // наприклад, validate не знайшов стовпець, якого немає в БД
            System.out.println("Не вдалося запустити Hibernate: " + rootMessage(e));
            return;
        }
        try (factory) {
            Queries queries = new Queries(factory);
            System.out.println("Підключено до БД: northwind_orm, MySQL " + queries.serverVersion() + " (Hibernate ORM)");
            printMenu();
            boolean running = true;
            while (running) {
                System.out.println();
                String choice = ask("Ваш вибір");
                try {
                    running = handle(choice, queries);
                } catch (RuntimeException e) {
                    System.out.println("Помилка: " + rootMessage(e));
                }
            }
        }
    }

    private static boolean handle(String choice, Queries queries) {
        switch (choice) {
            case "1" -> {
                System.out.println("1.categories 2.products 3.customers 4.employees 5.orders 6.order details 7.suppliers 8.shippers");
                System.out.println("9.region 10.territories 11.employee territories 12.customer demographics 13.customer demo");
                int table = Integer.parseInt(ask("Таблиця"));
                if (table < 1 || table > 13) {
                    System.out.println("Немає такої таблиці");
                } else {
                    queries.list(table - 1);
                }
            }
            case "3" -> queries.avgPriceByCategory();
            case "4" -> queries.employeesAndCustomersByCity();
            case "5" -> queries.revenueByCustomer();
            case "6" -> queries.ordersWithEmployee();
            case "7" -> queries.nPlusOne();
            case "9" -> printMenu();
            case "0" -> {
                System.out.println("Роботу завершено");
                return false;
            }
            default -> System.out.println("Немає такого пункту меню");
        }
        return true;
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
        System.out.println("1 Показати таблицю (13 таблиць, перші 10 рядків)");
        System.out.println("3 Запит 3: середня ціна по категоріях   4 Запит 4: співробітники й клієнти по містах");
        System.out.println("5 Запит 5: дохід від клієнтів   6 Запит 6: замовлення зі співробітником");
        System.out.println("7 Демонстрація N+1   9 Меню   0 Вихід");
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        // повідомлення Hibernate про схему лежить у верхньому винятку, у кореня воно зазвичай коротше
        return e.getMessage() != null && e.getMessage().startsWith("Schema-validation") ? e.getMessage() : t.getMessage();
    }
}
