package ua.duikt.lavrishko.reports.service;

import ua.duikt.lavrishko.reports.format.ReportFormat;
import ua.duikt.lavrishko.reports.source.DataSource;

/** Генерує звіт. Знає лише інтерфейси: формат і джерело приходять через конструктор. */
public class ReportService {

    private final ReportFormat format;
    private final DataSource source;

    public ReportService(ReportFormat format, DataSource source) {
        this.format = format;
        this.source = source;
    }

    public String generate() {
        return format.render(source.load());
    }
}
