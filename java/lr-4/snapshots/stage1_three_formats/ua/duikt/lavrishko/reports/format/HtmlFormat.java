package ua.duikt.lavrishko.reports.format;

import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** HTML-таблиця; підтримує сторінки. */
public class HtmlFormat extends AbstractPagedFormat {

    @Override
    protected String header(Table table) {
        return "<table>\n<caption>" + escape(table.title()) + "</caption>\n"
                + cells("th", table.headers()) + "\n";
    }

    @Override
    protected String formatRow(Table table, List<String> row) {
        return cells("td", row);
    }

    @Override
    protected String footer(Table table) {
        return "</table>\n";
    }

    @Override
    protected String pageMarker(int n, int total) {
        return "<!-- Сторінка " + n + " з " + total + " -->\n";
    }

    private static String cells(String tag, List<String> values) {
        StringBuilder row = new StringBuilder("<tr>");
        for (String v : values) {
            row.append('<').append(tag).append('>').append(escape(v)).append("</").append(tag).append('>');
        }
        return row.append("</tr>").toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
