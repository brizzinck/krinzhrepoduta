package ua.duikt.lavrishko.lr6;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Читає файл UTF-8, пропускає заголовок і кладе записи у сховище. */
public final class Loader {
    private Loader() {
    }

    /** Повертає кількість рядків з даними у файлі (без заголовка). */
    public static <K, T extends Keyed<K>> int load(Path path, LineParser<T> parser, Storage<K, T> storage)
            throws IOException {
        int lines = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine(); // заголовок
            String line;
            int number = 1;
            while ((line = reader.readLine()) != null) {
                number++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    storage.add(parser.parse(line));
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("рядок " + number + ": " + e.getMessage(), e);
                }
                lines++;
            }
        }
        return lines;
    }
}
