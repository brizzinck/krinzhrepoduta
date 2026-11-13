package ua.duikt.lavrishko.pr7;

import java.util.List;
import java.util.Optional;

/** Дослід 3: findFirst, Optional і orElse. Пошук без результату та за умовою досліду 2. */
public class Exp3Optional {
    public static void main(String[] args) {
        List<Issue> issues = Sample.issues();

        System.out.println("Пошук без результату (оцінка понад 100):");
        Optional<Issue> none = issues.stream().filter(i -> i.getStoryPoints() > 100).findFirst();
        System.out.println("  Optional: " + none);
        System.out.println("  isPresent: " + none.isPresent());
        System.out.println("  map(Issue::getKey).orElse: "
                + none.map(Issue::getKey).orElse("не знайдено"));

        System.out.println("Пошук за умовою досліду 2 (оцінка не менше 5):");
        Optional<Issue> found = issues.stream().filter(i -> i.getStoryPoints() >= 5).findFirst();
        System.out.println("  Optional: " + found);
        System.out.println("  isPresent: " + found.isPresent());
        System.out.println("  map(Issue::getKey).orElse: "
                + found.map(Issue::getKey).orElse("не знайдено"));
    }
}
