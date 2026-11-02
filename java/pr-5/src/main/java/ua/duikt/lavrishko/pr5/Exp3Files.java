package ua.duikt.lavrishko.pr5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Дослід 3: BufferedWriter без close() і з try-with-resources, читання в різних кодуваннях. */
public final class Exp3Files {

    /** Кілька рядків із кирилицею: їх і записуємо. */
    private static final String[] LINES = {
        "Привіт зі складу",
        "Артикул A-101: молоко, залишок 15",
        "Артикул A-102: хліб, залишок 40"
    };

    private Exp3Files() {
    }

    /** Пише рядки через BufferedWriter і НЕ закриває його. Буфер не скидається, файл лишається порожнім. */
    public static void writeWithoutClose(Path file) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        for (String line : LINES) {
            writer.write(line);
            writer.newLine();
        }
        System.out.println("Записано рядків: " + LINES.length + ", close() не викликано");
    }

    /** Те саме в try-with-resources: close() викликається автоматично й скидає буфер на диск. */
    public static void writeWithResources(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : LINES) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Записано рядків: " + LINES.length + ", close() викличе try-with-resources");
        }
    }

    /** Пише ті самі рядки в кодуванні windows-1251 (для читання «навпаки» як UTF-8). */
    public static void writeWindows1251(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("windows-1251"))) {
            for (String line : LINES) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Записано рядків у windows-1251: " + LINES.length);
        }
    }

    /**
     * Читає файл через BufferedReader із явним кодуванням. InputStreamReader замінює байти,
     * які не вдалося розкодувати, на символ U+FFFD, тому читання не переривається.
     */
    public static void read(Path file, String charsetName) throws IOException {
        Charset charset = Charset.forName(charsetName);
        System.out.println("Кодування читання: " + charset.name());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile()), charset))) {
            String line;
            int number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                System.out.println(number + ": " + line);
            }
            if (number == 0) {
                System.out.println("(файл порожній, рядків немає)");
            }
        }
    }
}
