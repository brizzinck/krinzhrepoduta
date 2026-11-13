package ua.duikt.lavrishko.pr6;

import java.util.List;

/** Дослід 2б: видалення з ArrayList прямо в циклі for-each. */
public class Exp2Cme {
    public static void main(String[] args) {
        List<Issue> issues = Exp2Iterator.sample();
        System.out.println("До видалення: " + issues.size() + " задач");
        for (Issue issue : issues) {
            if (issue.getStoryPoints() < 3) {
                issues.remove(issue);   // змінює список під час обходу
            }
        }
        System.out.println("Після видалення: " + issues.size() + " задач");
    }
}
