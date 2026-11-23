package ua.duikt.lavrishko.reports.format;

import java.util.ArrayList;
import java.util.List;
import ua.duikt.lavrishko.reports.model.Table;

/**
 * Основа форматів зі сторінками: цикл по сторінках записано один раз.
 * Формат лише повертає позначку «Сторінка N з M». CsvFormat цього класу не успадковує.
 */
public abstract class AbstractPagedFormat extends AbstractReportFormat implements PagedFormat {

    @Override
    public final List<String> renderPages(Table table, int pageSize) {
        List<Table> pages = Pagination.split(table, pageSize);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            result.add(render(pages.get(i)) + pageMarker(i + 1, pages.size()));
        }
        return result;
    }

    protected abstract String pageMarker(int n, int total);
}
