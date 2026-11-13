package ua.duikt.lavrishko.lr6;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Завантажує обидва файли варіанта 2 в те саме сховище й друкує звіт. */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Використання: Main <файл задач> <файл учасників>");
            System.exit(2);
        }
        Storage<String, Issue> issues = new Storage<>();
        int issueLines = Loader.load(Path.of(args[0]), new IssueParser(), issues);
        Storage<String, Member> members = new Storage<>();
        int memberLines = Loader.load(Path.of(args[1]), new MemberParser(), members);

        report("Задачі", issueLines, issues, new String[] {"SHOP-136", "SHOP-106", "SHOP-000"},
                "За story points", new ByStoryPoints());
        System.out.println();
        report("Учасники", memberLines, members, new String[] {"echo34", "falcon14", "nobody00"},
                "За кількістю закритих задач", new ByClosedCount());
        System.out.println();
        encapsulation(issues);
    }

    private static <T extends Keyed<String>> void report(String title, int lines, Storage<String, T> storage,
            String[] keys, String criterionTitle, Comparator<T> criterion) {
        System.out.println(title + ": рядків у файлі " + lines + ", записів у сховищі " + storage.size());
        System.out.println("Повтори ключа (лишається пізніший запис): " + storage.replacements().size());
        for (Replacement<T> r : storage.replacements()) {
            String state = r.before().equals(r.after()) ? "той самий запис" : "значення різні";
            System.out.println("  " + r.after().key() + ": " + state + "; було: " + r.before()
                    + "; стало: " + r.after());
        }
        for (String key : keys) {
            var found = storage.find(key);
            System.out.println("Пошук " + key + ": " + (found.isPresent() ? found.get() : "не знайдено"));
        }
        List<T> sorted = storage.sorted(criterion);
        System.out.println(criterionTitle + " (" + sorted.size() + " записів):");
        for (T record : sorted) {
            System.out.println("  " + record);
        }
        boolean complete = sorted.size() == storage.size() && storage.all().containsAll(sorted);
        System.out.println("Перевірка: у списку " + sorted.size() + ", у сховищі " + storage.size()
                + ", усі записи списку є в сховищі: " + (complete ? "так" : "ні"));
    }

    /** Спроби змінити сховище ззовні, повз його методи. */
    private static void encapsulation(Storage<String, Issue> issues) {
        System.out.println("Захист внутрішнього стану (записів у сховищі " + issues.size() + "):");
        try {
            var it = issues.iterator();
            it.next();
            it.remove();
        } catch (UnsupportedOperationException e) {
            System.out.println("  iterator().remove(): " + e);
        }
        try {
            issues.all().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("  all().clear(): " + e);
        }
        List<Issue> copy = issues.sorted(new ByStoryPoints());
        copy.clear();
        System.out.println("  очищення відсортованої копії: у копії " + copy.size()
                + ", у сховищі " + issues.size());
        Collection<Issue> view = issues.all();
        int before = view.size();
        issues.add(new Issue("SHOP-999", "Перевірка подання", 1));
        System.out.println("  подання all() бачить нові записи сховища: було " + before + ", стало " + view.size());
        int count = 0;
        for (Issue issue : issues) {
            count++;
        }
        System.out.println("  обхід for-each дав " + count + " записів");
    }
}
