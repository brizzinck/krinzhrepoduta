package ua.duikt.lavrishko.pr6;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Дослід 2в: те саме видалення через Iterator.remove(). */
public class Exp2Iterator {
    static List<Issue> sample() {
        int[] points = {5, 1, 8, 2, 13, 3, 21, 1};
        List<Issue> list = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            list.add(new Issue("SHOP-" + (i + 1), "Задача " + (i + 1), points[i]));
        }
        return list;
    }

    public static void main(String[] args) {
        List<Issue> issues = sample();
        System.out.println("До видалення: " + issues.size() + " задач");
        Iterator<Issue> it = issues.iterator();
        while (it.hasNext()) {
            Issue issue = it.next();
            if (issue.getStoryPoints() < 3) {
                it.remove();   // ітератор сам оновлює свій стан
            }
        }
        System.out.println("Після видалення: " + issues.size() + " задач");
        for (Issue issue : issues) {
            System.out.println("  " + issue);
        }
    }
}
