package ua.duikt.lavrishko.reports.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** Читає таблицю з CSV-файлу в UTF-8: перший рядок містить назви стовпців. */
public class CsvFileDataSource implements DataSource {

    private final Path path;
    private final String title;

    public CsvFileDataSource(Path path, String title) {
        this.path = path;
        this.title = title;
    }

    @Override
    public Table load() {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataSourceException("Не вдалося прочитати файл " + path, e);
        }
        if (lines.isEmpty()) {
            throw new DataSourceException("Файл " + path + " порожній");
        }
        List<String> headers = parseLine(lines.get(0));
        List<List<String>> rows = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (!line.isBlank()) {
                rows.add(parseLine(line));
            }
        }
        return new Table(title, headers, rows);
    }

    /** Розбирає один рядок CSV: коми всередині лапок і подвоєні лапки "" підтримуються. */
    static List<String> parseLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString());
        return cells;
    }
}
