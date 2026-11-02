package ua.duikt.lavrishko.importer.app;

import ua.duikt.lavrishko.importer.parse.RecordFormatException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Точка входу: java -jar importer.jar <вхідний файл> <файл звіту>.
 * Тут перехоплюються всі винятки, які зупиняють імпорт. Користувач бачить коротке повідомлення
 * і причину нижнього рівня, але не бачить стектрейсу.
 */
public final class Main {

    /** Скільки відхилених рядків показати в консолі. Повний перелік завжди пишеться у звіт. */
    private static final int CONSOLE_REJECT_LIMIT = 20;

    private static final int EXIT_OK = 0;
    private static final int EXIT_IMPORT_FAILED = 1;
    private static final int EXIT_USAGE = 2;
    private static final int EXIT_UNEXPECTED = 3;

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        if (args.length != 2) {
            System.err.println("Використання: java -jar importer.jar <вхідний файл> <файл звіту>");
            return EXIT_USAGE;
        }
        try {
            Path input = Path.of(args[0]);
            Path output = Path.of(args[1]);
            ConsolePrinter printer = new ConsolePrinter();
            ImportSummary summary = new StockImporter().importFile(input, output, printer);
            printer.printHidden();
            System.out.println("Імпортовано: " + summary.imported() + ", відхилено: " + summary.rejected()
                    + ". Загальна вартість залишків: " + summary.totalValue().toPlainString()
                    + ". Звіт записано у " + output);
            return EXIT_OK;
        } catch (InvalidPathException e) {
            System.err.println("Помилка: некоректний шлях \"" + e.getInput() + "\"");
            return EXIT_USAGE;
        } catch (ImportFailedException e) {
            System.err.println("Помилка: " + e.getMessage());
            printCause(e.getCause());
            return EXIT_IMPORT_FAILED;
        } catch (RuntimeException e) {
            // Помилка в самій програмі. Стектрейс не показуємо, але тип і повідомлення видно.
            System.err.println("Непередбачена помилка: " + e);
            return EXIT_UNEXPECTED;
        }
    }

    /** Показує, що сталося на нижньому рівні, одним рядком замість стектрейсу. */
    private static void printCause(Throwable cause) {
        if (cause != null) {
            System.err.println("  причина: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
    }

    /** Виводить перші CONSOLE_REJECT_LIMIT відхилених рядків, решту лише рахує. */
    private static final class ConsolePrinter implements Consumer<RecordFormatException> {

        private long count;

        @Override
        public void accept(RecordFormatException e) {
            count++;
            if (count <= CONSOLE_REJECT_LIMIT) {
                System.out.println("Рядок " + e.lineNumber() + ": " + e.getMessage());
            }
        }

        void printHidden() {
            if (count > CONSOLE_REJECT_LIMIT) {
                System.out.println("... і ще " + (count - CONSOLE_REJECT_LIMIT)
                        + " відхилених рядків (повний перелік у звіті)");
            }
        }
    }
}
