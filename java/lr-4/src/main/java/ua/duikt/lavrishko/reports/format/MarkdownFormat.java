package ua.duikt.lavrishko.reports.format;

import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** Markdown-таблиця: додано після створення сервісу, сервіс не змінювався. */
public class MarkdownFormat extends AbstractReportFormat {

    @Override
    protected String header(Table table) {
        String separator = "| " + join(table.headers().stream().map(h -> "---").toList(), " | ") + " |";
        return "### " + table.title() + "\n\n" + formatRow(table, table.headers()) + "\n" + separator + "\n";
    }

    @Override
    protected String formatRow(Table table, List<String> row) {
        return "| " + join(row.stream().map(v -> v.replace("|", "\\|")).toList(), " | ") + " |";
    }
}
