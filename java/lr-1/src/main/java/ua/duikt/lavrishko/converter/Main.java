package ua.duikt.lavrishko.converter;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public final class Main {

    private static final int LABEL_COLUMN = 6;
    private static final int COLUMN = 15;

    private Main() {
    }

    public static void main(String[] args) {
        // Кодування задано явно, щоб результат не залежав від налаштувань системи.
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        if (args.length == 1 && args[0].equals("--table")) {
            printTable(out);
            return;
        }
        // Locale.ROOT: дробова частина завжди через крапку.
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8)
                .useLocale(Locale.ROOT);
        new Menu(in, out).run();
    }

    /** Таблиця всіх напрямків: скільки одиниць стовпця в одній одиниці рядка. */
    private static void printTable(PrintStream out) {
        StringBuilder header = new StringBuilder(StringUtils.rightPad("од.", LABEL_COLUMN));
        for (AreaUnit to : AreaUnit.values()) {
            header.append(StringUtils.rightPad(to.label(), COLUMN));
        }
        out.println(header.toString().stripTrailing());
        for (AreaUnit from : AreaUnit.values()) {
            StringBuilder row = new StringBuilder(StringUtils.rightPad(from.label(), LABEL_COLUMN));
            for (AreaUnit to : AreaUnit.values()) {
                row.append(StringUtils.rightPad(Converter.format(Converter.convert(1, from, to)), COLUMN));
            }
            out.println(row.toString().stripTrailing());
        }
    }
}
