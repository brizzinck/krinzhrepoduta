package ua.duikt.lavrishko.reports.format;

import java.util.ArrayList;
import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** Спільний код розбиття таблиці на сторінки (щоб не дублювати його у форматах). */
final class Pagination {

    private Pagination() {
    }

    static List<Table> split(Table table, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("розмір сторінки має бути більшим за 0");
        }
        List<Table> pages = new ArrayList<>();
        for (int from = 0; from < table.rows().size(); from += pageSize) {
            int to = Math.min(from + pageSize, table.rows().size());
            pages.add(new Table(table.title(), table.headers(), table.rows().subList(from, to)));
        }
        if (pages.isEmpty()) {
            pages.add(table);
        }
        return pages;
    }
}
