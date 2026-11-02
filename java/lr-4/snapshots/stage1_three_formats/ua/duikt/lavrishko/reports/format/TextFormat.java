package ua.duikt.lavrishko.reports.format;

import java.util.ArrayList;
import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** Звичайний текст зі стовпцями однакової ширини; підтримує сторінки. */
public class TextFormat extends AbstractReportFormat implements PagedFormat {

    @Override
    protected String header(Table table) {
        String line = "-".repeat(totalWidth(table));
        return table.title() + "\n" + line + "\n" + formatRow(table, table.headers()) + "\n" + line + "\n";
    }

    @Override
    protected String formatRow(Table table, List<String> row) {
        List<Integer> widths = widths(table);
        List<String> padded = new ArrayList<>();
        for (int i = 0; i < row.size(); i++) {
            padded.add(String.format("%-" + widths.get(i) + "s", row.get(i)));
        }
        return join(padded, " | ").stripTrailing();
    }

    @Override
    protected String footer(Table table) {
        return "Рядків: " + table.rows().size() + "\n";
    }

    @Override
    public List<String> renderPages(Table table, int pageSize) {
        return renderPagesWith(table, pageSize, (n, total) -> "-- Сторінка " + n + " з " + total + " --\n");
    }

    private static List<Integer> widths(Table table) {
        List<Integer> widths = new ArrayList<>();
        for (int c = 0; c < table.headers().size(); c++) {
            int w = table.headers().get(c).length();
            for (List<String> row : table.rows()) {
                w = Math.max(w, row.get(c).length());
            }
            widths.add(w);
        }
        return widths;
    }

    private static int totalWidth(Table table) {
        return widths(table).stream().mapToInt(Integer::intValue).sum() + 3 * (table.headers().size() - 1);
    }
}
