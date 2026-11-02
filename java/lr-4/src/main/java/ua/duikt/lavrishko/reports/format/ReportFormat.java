package ua.duikt.lavrishko.reports.format;

import ua.duikt.lavrishko.reports.model.Table;

/** Роль «перетворити таблицю на текст звіту». Спільна для всіх форматів. */
public interface ReportFormat {
    String render(Table table);
}
