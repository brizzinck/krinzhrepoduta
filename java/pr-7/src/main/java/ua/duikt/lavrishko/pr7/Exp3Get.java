package ua.duikt.lavrishko.pr7;

import java.util.List;
import java.util.Optional;

/** Дослід 3б: get() на порожньому Optional. Виняток не перехоплюється навмисно. */
public class Exp3Get {
    public static void main(String[] args) {
        List<Issue> issues = Sample.issues();
        Optional<Issue> none = issues.stream().filter(i -> i.getStoryPoints() > 100).findFirst();
        Issue issue = none.get();   // порожній Optional: виняток
        System.out.println("Отримано: " + issue);
    }
}
