package ua.duikt.lavrishko.reports.source;

import ua.duikt.lavrishko.reports.model.Table;

/** Дані, передані в конструктор (у пам'яті). */
public class InMemoryDataSource implements DataSource {

    private final Table table;

    public InMemoryDataSource(Table table) {
        this.table = table;
    }

    @Override
    public Table load() {
        return table;
    }
}
