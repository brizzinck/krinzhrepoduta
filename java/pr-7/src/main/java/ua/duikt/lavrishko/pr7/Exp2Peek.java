package ua.duikt.lavrishko.pr7;

import java.util.List;
import java.util.stream.Stream;

/** Дослід 2б: peek у конвеєрі з термінальною операцією і без неї. Режим: terminal або lazy. */
public class Exp2Peek {
    public static void main(String[] args) {
        List<Issue> issues = Sample.issues();
        if (args.length == 1 && args[0].equals("terminal")) {
            int sum = issues.stream()
                    .filter(issue -> issue.getStoryPoints() >= 5)
                    .peek(issue -> System.out.println("peek " + issue.getKey()))
                    .mapToInt(Issue::getStoryPoints)
                    .sum();
            System.out.println("сума: " + sum);
        } else if (args.length == 1 && args[0].equals("lazy")) {
            // Конвеєр збережено у змінну, термінальної операції немає
            Stream<Issue> pipeline = issues.stream()
                    .filter(issue -> issue.getStoryPoints() >= 5)
                    .peek(issue -> System.out.println("peek " + issue.getKey()));
            System.out.println("Конвеєр створено, термінальної операції немає (peek не викликано)");
        } else {
            System.err.println("Використання: Exp2Peek terminal|lazy");
            System.exit(2);
        }
    }
}
