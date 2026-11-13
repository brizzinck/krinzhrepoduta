package ua.duikt.lavrishko.lr7;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Звіт за трекером задач: підсумки за статусами, топ-5 за story points, пошук за ключем. */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Використання: java -jar report.jar <файл з даними> <ключ задачі>");
            System.exit(2);
        }
        String searchKey = args[1];

        // Кожен рядок файлу (крім заголовка) стає об'єктом Issue
        List<Issue> all;
        try (Stream<String> lines = Files.lines(Path.of(args[0]), StandardCharsets.UTF_8)) {
            all = lines.skip(1)
                    .filter(line -> !line.isBlank())
                    .map(Issue::parse)
                    .toList();
        }
        // Повністю однакові рядки дають рівні records, distinct() залишає по одному
        List<Issue> issues = all.stream().distinct().toList();
        System.out.println("Рядків у файлі: " + all.size() + ", унікальних записів: " + issues.size());

        // Підсумки за статусами: TreeMap дає алфавітний порядок категорій
        Map<String, IntSummaryStatistics> byStatus = issues.stream()
                .collect(Collectors.groupingBy(Issue::status, TreeMap::new,
                        Collectors.summarizingInt(Issue::storyPoints)));
        System.out.println("Статуси (кількість, сума story points):");
        byStatus.forEach((status, stat) ->
                System.out.printf("  %-12s %3d %5d%n", status, stat.getCount(), stat.getSum()));

        // Топ-5: за story points від більшого до меншого, за рівних за ключем
        System.out.println("Топ-5 за story points:");
        issues.stream()
                .sorted(Comparator.comparingInt(Issue::storyPoints).reversed()
                        .thenComparing(Issue::key))
                .limit(5)
                .forEach(issue -> System.out.printf("  %2d %s%n", issue.storyPoints(), issue.key()));

        // Пошук: Optional замість null
        String found = issues.stream()
                .filter(issue -> issue.key().equals(searchKey))
                .findFirst()
                .map(Issue::toString)
                .orElse("не знайдено");
        System.out.println("Пошук " + searchKey + ": " + found);
    }
}
