import java.util.List;
import ua.duikt.lavrishko.reports.format.CsvFormat;
import ua.duikt.lavrishko.reports.model.Table;
import ua.duikt.lavrishko.reports.service.PagedReportService;
import ua.duikt.lavrishko.reports.source.InMemoryDataSource;

class CsvPaged {
    public static void main(String[] args) {
        InMemoryDataSource source = new InMemoryDataSource(new Table("t", List.of("a"), List.of()));
        // CSV не підтримує сторінок, тому не є PagedFormat
        new PagedReportService(new CsvFormat(), source);
    }
}
