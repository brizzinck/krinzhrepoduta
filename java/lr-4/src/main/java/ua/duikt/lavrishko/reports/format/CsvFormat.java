package ua.duikt.lavrishko.reports.format;

import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** CSV: без назви й підсумку; сторінок немає (немає й методів для них). */
public class CsvFormat extends AbstractReportFormat {

    @Override
    protected String header(Table table) {
        return formatRow(table, table.headers()) + "\n";
    }

    @Override
    protected String formatRow(Table table, List<String> row) {
        return join(row.stream().map(CsvFormat::escape).toList(), ",");
    }

    /** Значення з комою, лапками або переносом беруться в лапки, лапки подвоюються. */
    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
