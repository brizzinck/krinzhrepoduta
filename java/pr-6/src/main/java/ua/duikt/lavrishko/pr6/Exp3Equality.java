package ua.duikt.lavrishko.pr6;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Дослід 3: HashSet і контракт equals/hashCode, потім зміна ключа вже вкладеного об'єкта. */
public class Exp3Equality {
    /** Копія Issue без equals і hashCode: рівність за посиланням. */
    static class IssueNoEq {
        final String key;
        final String title;
        final int storyPoints;

        IssueNoEq(String key, String title, int storyPoints) {
            this.key = key;
            this.title = title;
            this.storyPoints = storyPoints;
        }
    }

    /** Копія Issue, де перевизначено лише equals (за ключем). */
    static class IssueEqOnly {
        final String key;
        final String title;
        final int storyPoints;

        IssueEqOnly(String key, String title, int storyPoints) {
            this.key = key;
            this.title = title;
            this.storyPoints = storyPoints;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof IssueEqOnly other && Objects.equals(key, other.key);
        }
    }

    public static void main(String[] args) {
        Set<IssueNoEq> noEq = new HashSet<>();
        noEq.add(new IssueNoEq("SHOP-1", "Вхід через Google", 5));
        noEq.add(new IssueNoEq("SHOP-1", "Вхід через Google", 5));
        report("без equals і hashCode", noEq.size(), noEq.contains(new IssueNoEq("SHOP-1", "Вхід через Google", 5)));

        Set<IssueEqOnly> eqOnly = new HashSet<>();
        eqOnly.add(new IssueEqOnly("SHOP-1", "Вхід через Google", 5));
        eqOnly.add(new IssueEqOnly("SHOP-1", "Вхід через Google", 5));
        report("лише equals", eqOnly.size(), eqOnly.contains(new IssueEqOnly("SHOP-1", "Вхід через Google", 5)));

        Set<Issue> both = new HashSet<>();
        both.add(new Issue("SHOP-1", "Вхід через Google", 5));
        both.add(new Issue("SHOP-1", "Вхід через Google (дубль)", 5));
        report("equals і hashCode", both.size(), both.contains(new Issue("SHOP-1", "", 0)));

        // Змінюємо ключ об'єкта, що вже лежить у множині
        Set<Issue> set = new HashSet<>();
        Issue issue = new Issue("SHOP-1", "Вхід через Google", 5);
        set.add(issue);
        System.out.println();
        System.out.println("Зміна ключа елемента, що вже лежить у HashSet<Issue>:");
        System.out.println("  до зміни:  contains(SHOP-1) = " + set.contains(new Issue("SHOP-1", "", 0)));
        issue.setKey("SHOP-2");
        System.out.println("  після setKey(SHOP-2):");
        System.out.println("    contains(старий ключ SHOP-1) = " + set.contains(new Issue("SHOP-1", "", 0)));
        System.out.println("    contains(новий ключ SHOP-2)  = " + set.contains(new Issue("SHOP-2", "", 0)));
        System.out.println("    size = " + set.size() + ", у обході множини: " + set.iterator().next());
    }

    private static void report(String state, int size, boolean contains) {
        System.out.printf("%-24s size = %d, contains(третій рівний) = %b%n", state + ":", size, contains);
    }
}
