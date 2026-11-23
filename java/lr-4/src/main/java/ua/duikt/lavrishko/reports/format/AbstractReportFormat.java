package ua.duikt.lavrishko.reports.format;

import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/**
 * Спільна частина всіх форматів: послідовність «заголовок, рядки, підсумок» (шаблонний метод).
 * Конкретні формати реалізують лише те, що в них відрізняється.
 */
public abstract class AbstractReportFormat implements ReportFormat {

    @Override
    public final String render(Table table) {
        StringBuilder out = new StringBuilder(header(table));
        for (List<String> row : table.rows()) {
            out.append(formatRow(table, row)).append('\n');
        }
        out.append(footer(table));
        return out.toString();
    }

    protected abstract String header(Table table);

    protected abstract String formatRow(Table table, List<String> row);

    /** Підсумок за замовчуванням порожній; формати перевизначають за потреби. */
    protected String footer(Table table) {
        return "";
    }

    protected static String join(List<String> cells, String separator) {
        return String.join(separator, cells);
    }
}
