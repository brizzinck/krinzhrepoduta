package ua.duikt.lavrishko.reports.format;

import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/** Додаткова можливість: розбиття на сторінки. Реалізують лише формати, яким воно має сенс. */
public interface PagedFormat extends ReportFormat {
    List<String> renderPages(Table table, int pageSize);
}
