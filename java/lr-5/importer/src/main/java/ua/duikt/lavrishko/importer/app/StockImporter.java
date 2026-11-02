package ua.duikt.lavrishko.importer.app;

import ua.duikt.lavrishko.importer.domain.StockItem;
import ua.duikt.lavrishko.importer.parse.RecordFormatException;
import ua.duikt.lavrishko.importer.parse.StockItemParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Читає вхідний файл рядок за рядком і паралельно пише звіт.
 * Помилка в окремому рядку (RecordFormatException) лише відхиляє цей рядок.
 * Помилка файлу (ImportFailedException) зупиняє імпорт.
 */
public final class StockImporter {

    private static final char BOM = '﻿';

    private final StockItemParser parser = new StockItemParser();

    public ImportSummary importFile(Path input, Path output, Consumer<RecordFormatException> onRejected)
            throws ImportFailedException {
        checkNotSameFile(input, output);
        ImportSummary summary = new ImportSummary();
        // try-with-resources закриває обидва файли в будь-якому разі: і після успіху, і після винятку.
        // Ресурси закриваються у зворотному порядку: спершу writer, потім reader.
        try (BufferedReader reader = openInput(input);
             BufferedWriter writer = openOutput(output)) {

            writeLine(writer, "Звіт імпорту залишків товарів", output);
            writeLine(writer, "Вхідний файл: " + input, output);
            writeLine(writer, "", output);
            writeLine(writer, "Відхилені рядки:", output);

            readHeader(reader, input);
            long lineNumber = 1;
            String line;
            // Потокове читання: у пам'яті завжди лише один рядок файлу.
            while ((line = readLine(reader, input, lineNumber + 1)) != null) {
                lineNumber++;
                try {
                    StockItem item = parser.parse(line, lineNumber);
                    summary.accept(item);
                } catch (RecordFormatException e) {
                    summary.reject();
                    writeLine(writer, "Рядок " + e.lineNumber() + ": " + e.getMessage(), output);
                    onRejected.accept(e);
                }
            }
            if (summary.rejected() == 0) {
                writeLine(writer, "немає", output);
            }

            writeLine(writer, "", output);
            writeLine(writer, "Підсумок", output);
            writeLine(writer, "Рядків даних: " + summary.dataLines(), output);
            writeLine(writer, "Імпортовано позицій: " + summary.imported(), output);
            writeLine(writer, "Відхилено рядків: " + summary.rejected(), output);
            writeLine(writer, "Загальна вартість залишків: " + summary.totalValue().toPlainString(), output);
        } catch (IOException e) {
            // Сюди потрапляє лише помилка close(): writer під час закриття дописує буфер на диск.
            throw new ImportFailedException("не вдалося дописати звіт у файл " + output, e);
        }
        return summary;
    }

    private static void checkNotSameFile(Path input, Path output) throws ImportFailedException {
        try {
            if (Files.exists(input) && Files.exists(output) && Files.isSameFile(input, output)) {
                throw new ImportFailedException("вхідний і вихідний файл збігаються: " + input);
            }
        } catch (IOException e) {
            throw new ImportFailedException("не вдалося перевірити шляхи " + input + " і " + output, e);
        }
    }

    private static BufferedReader openInput(Path input) throws ImportFailedException {
        try {
            // Кодування вказано явно, тож результат не залежить від налаштувань ОС.
            return Files.newBufferedReader(input, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ImportFailedException("файл " + input + " не знайдено", e);
        } catch (AccessDeniedException e) {
            throw new ImportFailedException("немає доступу до файлу " + input, e);
        } catch (IOException e) {
            throw new ImportFailedException("не вдалося відкрити файл " + input, e);
        }
    }

    private static BufferedWriter openOutput(Path output) throws ImportFailedException {
        try {
            return Files.newBufferedWriter(output, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ImportFailedException("неможливо створити звіт " + output + ": теки не існує", e);
        } catch (AccessDeniedException e) {
            throw new ImportFailedException("немає права запису у файл " + output, e);
        } catch (IOException e) {
            throw new ImportFailedException("неможливо записати звіт у файл " + output, e);
        }
    }

    private static void readHeader(BufferedReader reader, Path input) throws ImportFailedException {
        String header = readLine(reader, input, 1);
        if (header == null) {
            throw new ImportFailedException("файл " + input + " порожній");
        }
        if (!header.isEmpty() && header.charAt(0) == BOM) {
            header = header.substring(1);
        }
        if (!StockItemParser.HEADER.equals(header.strip())) {
            throw new ImportFailedException("файл " + input + " має неочікуваний заголовок \"" + header
                    + "\", очікувався \"" + StockItemParser.HEADER + "\"");
        }
    }

    private static String readLine(BufferedReader reader, Path input, long lineNumber)
            throws ImportFailedException {
        try {
            return reader.readLine();
        } catch (CharacterCodingException e) {
            throw new ImportFailedException("файл " + input + " не в кодуванні UTF-8 (рядок " + lineNumber + ")", e);
        } catch (IOException e) {
            throw new ImportFailedException("помилка читання файлу " + input + " (рядок " + lineNumber + ")", e);
        }
    }

    private static void writeLine(BufferedWriter writer, String text, Path output) throws ImportFailedException {
        try {
            writer.write(text);
            writer.newLine();
        } catch (IOException e) {
            throw new ImportFailedException("помилка запису у файл " + output, e);
        }
    }
}
