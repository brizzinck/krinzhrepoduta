package ua.duikt.lavrishko.reports.source;

import ua.duikt.lavrishko.reports.model.Table;

/** Джерело даних звіту (друга залежність сервісу). */
public interface DataSource {
    Table load();
}
