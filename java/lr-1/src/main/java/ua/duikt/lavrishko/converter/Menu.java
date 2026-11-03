package ua.duikt.lavrishko.converter;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.util.Scanner;

/** Консольне меню. Введення перевіряється методами hasNext..., винятки не використовуються. */
public final class Menu {

    private static final int EXIT = 0;
    private static final int AREA = 1;
    private static final String NOT_A_NUMBER = "Помилка: введіть число. Спробуйте ще раз.";

    private final Scanner in;
    private final PrintStream out;

    public Menu(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println("Конвертер одиниць");
        if (chooseQuantity() == AREA) {
            printUnits();
            convertLoop();
        }
        out.println("Вихід. Дякуємо за використання!");
    }

    /** Повертає номер величини або EXIT, також коли введення закінчилось. */
    private int chooseQuantity() {
        while (true) {
            out.println("1) Площа    0) Вихід");
            out.print("Вибір: ");
            if (!in.hasNext()) {
                return EXIT;
            }
            if (!in.hasNextInt()) {
                skipLine();
                out.println(NOT_A_NUMBER);
                continue;
            }
            int choice = in.nextInt();
            if (choice == AREA || choice == EXIT) {
                return choice;
            }
            out.println("Помилка: такого пункту немає. Спробуйте ще раз.");
        }
    }

    private void printUnits() {
        out.println("Одиниці (номер, назва, площа в m²):");
        AreaUnit[] units = AreaUnit.values();
        for (int i = 0; i < units.length; i++) {
            out.println((i + 1) + ") " + StringUtils.rightPad(units[i].label(), 6)
                    + Converter.format(units[i].squareMetres()));
        }
    }

    private void convertLoop() {
        while (true) {
            out.println();
            Double value = readValue();
            if (value == null || value == 0) {
                return;
            }
            AreaUnit from = readUnit("З");
            AreaUnit to = from == null ? null : readUnit("У");
            if (to == null) {
                return;
            }
            double result = Converter.convert(value, from, to);
            // Велике значення може вийти за межі double вже під час множення.
            if (!Double.isFinite(result)) {
                out.println("Помилка: результат завеликий. Спробуйте менше значення.");
                continue;
            }
            out.println(Converter.format(value) + " " + from.label() + " = "
                    + Converter.format(result) + " " + to.label());
        }
    }

    /** Коректне значення, або null, коли введення закінчилось. */
    private Double readValue() {
        while (true) {
            out.print("Значення (0 для виходу): ");
            if (!in.hasNext()) {
                return null;
            }
            if (!in.hasNextDouble()) {
                skipLine();
                out.println(NOT_A_NUMBER);
                continue;
            }
            double value = in.nextDouble();
            // hasNextDouble приймає NaN, Infinity і 1e999, тому скінченність перевіряємо окремо.
            if (!Double.isFinite(value)) {
                out.println(NOT_A_NUMBER);
            } else if (value < 0) {
                out.println(
                        "Помилка: площа не може бути від'ємною. Спробуйте ще раз.");
            } else {
                return value;
            }
        }
    }

    /** Одиниця за номером або назвою, або null, коли введення закінчилось. */
    private AreaUnit readUnit(String prompt) {
        while (true) {
            out.print(prompt + " (номер або назва): ");
            if (!in.hasNext()) {
                return null;
            }
            AreaUnit unit = in.hasNextInt() ? AreaUnit.byNumber(in.nextInt()) : AreaUnit.byName(in.next());
            if (unit != null) {
                return unit;
            }
            out.println("Помилка: такої одиниці немає. Спробуйте ще раз.");
        }
    }

    /**
     * Відкидає хибне значення й решту його рядка, щоб одна помилка дала одне повідомлення.
     * Самого nextLine мало: після nextInt у буфері лишається кінець попереднього рядка.
     */
    private void skipLine() {
        in.next();
        if (in.hasNextLine()) {
            in.nextLine();
        }
    }
}
