package ua.duikt.lavrishko.pr7;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Дослід 1: сортування за story points трьома способами. */
public class Exp1Sort {
    public static void main(String[] args) {
        // 1. Анонімний клас
        List<Issue> byAnonymous = new ArrayList<>(Sample.issues());
        byAnonymous.sort(new Comparator<Issue>() {
            @Override
            public int compare(Issue a, Issue b) {
                return Integer.compare(a.getStoryPoints(), b.getStoryPoints());
            }
        });
        print("Анонімний клас", byAnonymous);

        // 2. Лямбда-вираз
        List<Issue> byLambda = new ArrayList<>(Sample.issues());
        byLambda.sort((a, b) -> Integer.compare(a.getStoryPoints(), b.getStoryPoints()));
        print("Лямбда-вираз", byLambda);

        // 3. Посилання на метод доступу
        List<Issue> byReference = new ArrayList<>(Sample.issues());
        byReference.sort(Comparator.comparingInt(Issue::getStoryPoints));
        print("Comparator.comparingInt", byReference);

        // Порядок однаковий і для рівних оцінок, бо сортування стабільне
        System.out.println("Порядок збігається: " + (keys(byAnonymous).equals(keys(byLambda))
                && keys(byLambda).equals(keys(byReference))));
    }

    private static void print(String title, List<Issue> list) {
        System.out.println(title + " (" + list.size() + " рядків):");
        for (Issue issue : list) {
            System.out.println("  " + issue);
        }
    }

    private static List<String> keys(List<Issue> list) {
        List<String> keys = new ArrayList<>();
        for (Issue issue : list) {
            keys.add(issue.getKey());
        }
        return keys;
    }
}
