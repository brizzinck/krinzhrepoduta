package rental;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;

/** Консольне меню додатку «Прокат автомобілів» на Jdbi. CRUD для branches, customers, cars і три запити. */
public class Main {
    private static final Scanner IN = new Scanner(System.in, StandardCharsets.UTF_8);
    // якщо ввід іде з файлу, друкуємо його, щоб вивід читався як сеанс роботи
    private static final boolean ECHO = System.console() == null;

    public static void main(String[] args) {
        Jdbi jdbi = Database.connect();
        BranchDao branches = jdbi.onDemand(BranchDao.class);
        CustomerDao customers = jdbi.onDemand(CustomerDao.class);
        CarDao cars = jdbi.onDemand(CarDao.class);
        ReportDao reports = jdbi.onDemand(ReportDao.class);
        System.out.println("Підключено до БД: " + jdbi.withHandle(h -> h.createQuery("SELECT DATABASE()").mapTo(String.class).one()));
        printMenu();
        boolean running = true;
        while (running) {
            System.out.println();
            String choice = ask("Ваш вибір");
            // кожна операція в окремому try: помилка БД не завершує програму
            try {
                running = handle(choice, branches, customers, cars, reports);
            } catch (JdbiException e) {
                System.out.println(describe(e));
            } catch (NumberFormatException | DateTimeParseException e) {
                System.out.println("Помилка вводу: неправильне число або дата");
            }
        }
    }

    private static boolean handle(String choice, BranchDao branches, CustomerDao customers, CarDao cars,
                                  ReportDao reports) {
        switch (choice) {
            case "1" -> list(askTable(), branches, customers, cars);
            case "2" -> find(askTable(), branches, customers, cars);
            case "3" -> create(askTable(), branches, customers, cars);
            case "4" -> update(askTable(), branches, customers, cars);
            case "5" -> delete(askTable(), branches, customers, cars);
            case "6" -> Printer.print(reports.activeRentals(), ActiveRental.class);
            case "7" -> Printer.print(
                reports.freeCars(ask("Місто"), new BigDecimal(ask("Максимальна ціна за добу"))), FreeCar.class);
            case "8" -> Printer.print(reports.branchRevenue(), BranchRevenue.class);
            case "9" -> printMenu();
            case "0" -> {
                System.out.println("Роботу завершено");
                return false;
            }
            default -> System.out.println("Немає такого пункту меню");
        }
        return true;
    }

    private static void list(int table, BranchDao branches, CustomerDao customers, CarDao cars) {
        switch (table) {
            case 1 -> Printer.print(branches.findAll(), Branch.class);
            case 2 -> Printer.print(customers.findAll(), Customer.class);
            default -> Printer.print(cars.findAll(), Car.class);
        }
    }

    private static void find(int table, BranchDao branches, CustomerDao customers, CarDao cars) {
        int id = askInt("Ключ");
        switch (table) {
            case 1 -> showOne(branches.findById(id), Branch.class);
            case 2 -> showOne(customers.findById(id), Customer.class);
            default -> showOne(cars.findById(id), Car.class);
        }
    }

    private static <T extends Record> void showOne(Optional<T> row, Class<T> type) {
        if (row.isPresent()) {
            Printer.print(List.of(row.get()), type);
        } else {
            System.out.println("Рядок не знайдено");
        }
    }

    private static void create(int table, BranchDao branches, CustomerDao customers, CarDao cars) {
        System.out.println("Порожнє значення: типове (0, available) або NULL");
        int key;
        switch (table) {
            case 1 -> key = branches.insert(new Branch(0, ask("name"), ask("city"), ask("address"), ask("phone")));
            case 2 -> key = customers.insert(new Customer(0, ask("first_name"), ask("last_name"),
                emptyToNull(ask("middle_name")), ask("phone"), ask("email"), LocalDate.parse(ask("birth_date")),
                ask("license_number"), LocalDate.parse(ask("license_expires")), null));
            default -> key = cars.insert(new Car(0, askInt("model_id"), askInt("branch_id"), ask("vin"),
                ask("plate_number"), askInt("manufacture_year"), orDefault(ask("mileage_km"), "0", Long::parseLong),
                new BigDecimal(ask("daily_price")), orDefault(ask("status"), "available", s -> s)));
        }
        System.out.println("Додано рядок, ключ: " + key);
    }

    /** Змінюємо запис, який уже прочитав Jdbi: порожнє значення залишає поточне. */
    private static void update(int table, BranchDao branches, CustomerDao customers, CarDao cars) {
        int id = askInt("Ключ");
        System.out.println("Порожнє значення: стовпець не змінюється");
        int changed;
        switch (table) {
            case 1 -> {
                Optional<Branch> old = branches.findById(id);
                if (old.isEmpty()) {
                    System.out.println("Рядок не знайдено");
                    return;
                }
                Branch b = old.get();
                changed = branches.update(new Branch(id, keep("name", b.name()), keep("city", b.city()),
                    keep("address", b.address()), keep("phone", b.phone())));
            }
            case 2 -> {
                Optional<Customer> old = customers.findById(id);
                if (old.isEmpty()) {
                    System.out.println("Рядок не знайдено");
                    return;
                }
                Customer c = old.get();
                changed = customers.update(new Customer(id, keep("first_name", c.firstName()),
                    keep("last_name", c.lastName()), c.middleName(), keep("phone", c.phone()),
                    keep("email", c.email()), c.birthDate(), keep("license_number", c.licenseNumber()),
                    LocalDate.parse(keep("license_expires", c.licenseExpires().toString())), c.registeredAt()));
            }
            default -> {
                Optional<Car> old = cars.findById(id);
                if (old.isEmpty()) {
                    System.out.println("Рядок не знайдено");
                    return;
                }
                Car c = old.get();
                changed = cars.update(new Car(id, c.modelId(), c.branchId(), c.vin(), keep("plate_number", c.plateNumber()),
                    c.manufactureYear(), Long.parseLong(keep("mileage_km", String.valueOf(c.mileageKm()))),
                    new BigDecimal(keep("daily_price", c.dailyPrice().toPlainString())), keep("status", c.status())));
            }
        }
        System.out.println("Змінено рядків: " + changed);
    }

    private static void delete(int table, BranchDao branches, CustomerDao customers, CarDao cars) {
        int id = askInt("Ключ");
        int deleted = switch (table) {
            case 1 -> branches.delete(id);
            case 2 -> customers.delete(id);
            default -> cars.delete(id);
        };
        System.out.println("Видалено рядків: " + deleted);
    }

    private static int askTable() {
        System.out.println("1.branches  2.customers  3.cars");
        int number = askInt("Таблиця");
        if (number < 1 || number > 3) {
            throw new NumberFormatException();
        }
        return number;
    }

    private static int askInt(String prompt) {
        return Integer.parseInt(ask(prompt));
    }

    private static String keep(String column, String current) {
        String value = ask(column + " [" + current + "]");
        return value.isEmpty() ? current : value;
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private static <T> T orDefault(String value, String fallback, java.util.function.Function<String, T> parse) {
        return parse.apply(value.isEmpty() ? fallback : value);
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

    /** Jdbi загортає SQLException; код помилки MySQL беремо з причини. */
    private static String describe(JdbiException e) {
        Throwable cause = e.getCause();
        if (!(cause instanceof SQLException sql)) {
            return "Помилка Jdbi: " + e.getMessage();
        }
        String reason = switch (sql.getErrorCode()) {
            case 1062 -> "таке значення вже є (UNIQUE)";
            case 1451 -> "на рядок посилаються інші таблиці (FOREIGN KEY)";
            case 1452 -> "немає рядка, на який іде посилання (FOREIGN KEY)";
            case 3819 -> "порушено обмеження CHECK";
            case 1048, 1364 -> "не заповнено обов'язковий стовпець (NOT NULL)";
            default -> "помилка БД";
        };
        return "Помилка " + sql.getErrorCode() + ": " + reason + ". " + sql.getMessage();
    }
}
