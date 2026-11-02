package ua.duikt.lavrishko.reports.app;

import java.nio.file.Path;
import java.util.List;
import ua.duikt.lavrishko.reports.format.CsvFormat;
import ua.duikt.lavrishko.reports.format.HtmlFormat;
import ua.duikt.lavrishko.reports.format.TextFormat;
import ua.duikt.lavrishko.reports.model.Table;
import ua.duikt.lavrishko.reports.service.PagedReportService;
import ua.duikt.lavrishko.reports.service.ReportService;
import ua.duikt.lavrishko.reports.source.CsvFileDataSource;
import ua.duikt.lavrishko.reports.source.DataSource;
import ua.duikt.lavrishko.reports.source.InMemoryDataSource;

/** Єдина точка збирання: тут створюються й з'єднуються всі об'єкти. */
public class Main {

    public static void main(String[] args) {
        Path csv = Path.of(args.length > 0 ? args[0] : "data/rentals.csv");

        DataSource memory = new InMemoryDataSource(new Table("Парк авто",
                List.of("Авто", "Рік", "Статус"),
                List.of(List.of("Skoda Octavia", "2022", "вільне"),
                        List.of("Toyota Camry", "2021", "в оренді"),
                        List.of("Renault Duster", "2023", "вільне"))));
        DataSource file = new CsvFileDataSource(csv, "Оренди за тиждень");

        System.out.println("Конфігурація 1: текст + дані в пам'яті");
        System.out.print(new ReportService(new TextFormat(), memory).generate());

        System.out.println("Конфігурація 2: CSV + дані з файлу");
        System.out.print(new ReportService(new CsvFormat(), file).generate());

        System.out.println("Конфігурація 3: HTML + дані в пам'яті");
        System.out.print(new ReportService(new HtmlFormat(), memory).generate());

        System.out.println("Конфігурація 4: текст + дані з файлу (формат і джерело замінено незалежно)");
        System.out.print(new ReportService(new TextFormat(), file).generate());

        System.out.println("Конфігурація 5: HTML зі сторінками по 3 рядки + дані з файлу");
        new PagedReportService(new HtmlFormat(), file).generatePages(3).forEach(System.out::print);
    }
}
