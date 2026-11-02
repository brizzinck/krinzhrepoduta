package ua.duikt.lavrishko.reports.model;

import java.util.List;

/** Табличні дані звіту: заголовок, назви стовпців і рядки. Незмінний. */
public record Table(String title, List<String> headers, List<List<String>> rows) {

    public Table {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("таблиця має містити хоча б один стовпець");
        }
        headers = List.copyOf(headers);
        rows = rows.stream().map(List::copyOf).toList();
        for (List<String> row : rows) {
            if (row.size() != headers.size()) {
                throw new IllegalArgumentException(
                        "рядок має " + row.size() + " значень, а стовпців " + headers.size());
            }
        }
    }
}
