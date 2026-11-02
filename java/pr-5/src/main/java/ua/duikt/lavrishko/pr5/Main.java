package ua.duikt.lavrishko.pr5;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Запускач дослідів. Перший аргумент задає дослід, другий режим:
 * java -cp target/classes ua.duikt.lavrishko.pr5.Main exp1 catch data/stock.txt
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Використання: Main <exp1|exp2|exp3> <режим> [аргументи]");
            System.exit(2);
        }
        String mode = args[1];
        switch (args[0]) {
            case "exp1" -> runExp1(mode, args);
            case "exp2" -> runExp2(mode, args);
            case "exp3" -> runExp3(mode, args);
            default -> {
                System.err.println("Невідомий дослід: " + args[0]);
                System.exit(2);
            }
        }
    }

    private static void runExp1(String mode, String[] args) throws IOException {
        switch (mode) {
            case "catch" -> Exp1Checked.readFirstLineWithCatch(args[2]);
            // Виняток із throws не обробляється й у main: він виходить за межі програми
            case "throws" -> Exp1Checked.openWithThrows(args[2]);
            case "div" -> System.out.println("Результат: " + Exp1Checked.divide(10, Integer.parseInt(args[2])));
            case "array" -> {
                int[] stock = {5, 10, 15};
                System.out.println("Елемент: " + Exp1Checked.elementAt(stock, Integer.parseInt(args[2])));
            }
            case "finally" -> Exp1Checked.readWithFinally(args[2]);
            case "finally-return" -> System.out.println("Метод повернув: " + Exp1Checked.returnFromTry());
            default -> throw new IllegalArgumentException("Невідомий режим: " + mode);
        }
    }

    /** Виконує розбір, а перехоплений виняток друкує зі стектрейсом і значенням getCause(). */
    private static void runExp2(String mode, String[] args) {
        String text = args[2];
        try {
            int quantity = switch (mode) {
                case "no-cause" -> Exp2Cause.parseQuantityWithoutCause(text);
                case "with-cause" -> Exp2Cause.parseQuantityWithCause(text);
                default -> throw new IllegalArgumentException("Невідомий режим: " + mode);
            };
            System.out.println("Результат: " + quantity);
        } catch (InvalidQuantityException e) {
            e.printStackTrace(System.out);
            System.out.println("getMessage(): " + e.getMessage());
            System.out.println("getCause():   " + e.getCause());
        }
    }

    private static void runExp3(String mode, String[] args) throws IOException {
        Path file = Path.of(args[2]);
        switch (mode) {
            case "write-noclose" -> Exp3Files.writeWithoutClose(file);
            case "write-twr" -> Exp3Files.writeWithResources(file);
            case "write-cp1251" -> Exp3Files.writeWindows1251(file);
            case "read" -> Exp3Files.read(file, args[3]);
            default -> throw new IllegalArgumentException("Невідомий режим: " + mode);
        }
    }
}
