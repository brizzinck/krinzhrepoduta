package ua.duikt.lavrishko.reports.service;

import java.util.List;
import ua.duikt.lavrishko.reports.format.PagedFormat;
import ua.duikt.lavrishko.reports.source.DataSource;

/** Сервіс для форматів із розбиттям на сторінки: приймає лише PagedFormat. */
public class PagedReportService {

    private final PagedFormat format;
    private final DataSource source;

    public PagedReportService(PagedFormat format, DataSource source) {
        this.format = format;
        this.source = source;
    }

    public List<String> generatePages(int pageSize) {
        return format.renderPages(source.load(), pageSize);
    }
}
