import java.util.List;
import ua.duikt.lavrishko.reports.format.TextFormat;
import ua.duikt.lavrishko.reports.model.Table;
import ua.duikt.lavrishko.reports.service.PagedReportService;
import ua.duikt.lavrishko.reports.source.InMemoryDataSource;

class TextPaged {
    public static void main(String[] args) {
        InMemoryDataSource source = new InMemoryDataSource(new Table("t", List.of("a"), List.of()));
        // текстовий формат підтримує сторінки: те саме з TextFormat компілюється
        new PagedReportService(new TextFormat(), source);
    }
}
