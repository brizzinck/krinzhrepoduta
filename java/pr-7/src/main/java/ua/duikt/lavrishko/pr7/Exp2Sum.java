package ua.duikt.lavrishko.pr7;

import java.util.List;

/** Дослід 2а: сума story points задач з оцінкою не менше 5, цикл проти потоку. */
public class Exp2Sum {
    static int sumWithLoop(List<Issue> issues) {
        int sum = 0;
        for (Issue issue : issues) {
            if (issue.getStoryPoints() >= 5) {
                sum += issue.getStoryPoints();
            }
        }
        return sum;
    }

    static int sumWithStream(List<Issue> issues) {
        return issues.stream()
                .filter(issue -> issue.getStoryPoints() >= 5)
                .mapToInt(Issue::getStoryPoints)
                .sum();
    }

    public static void main(String[] args) {
        List<Issue> issues = Sample.issues();
        System.out.println("цикл: " + sumWithLoop(issues) + "   потік: " + sumWithStream(issues));
    }
}
