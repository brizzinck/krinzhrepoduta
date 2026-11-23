package ua.duikt.lavrishko.pr6;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** Дослід 2а: час 1000 вставок у середину списків і 1000 викликів contains, 5 повторів. */
public class Exp2Timing {
    private static final int SIZE = 100_000;
    private static final int OPS = 1_000;
    private static final int REPEATS = 5;

    public static void main(String[] args) {
        System.out.println("Елементів: " + SIZE + ", операцій: " + OPS + ", час у мс");
        System.out.printf("%-8s %-14s %-14s %-14s %-14s %-14s%n", "Запуск",
                "ArrayList ins", "LinkedList ins", "ArrayList has", "LinkedList has", "HashSet has");
        for (int run = 1; run <= REPEATS; run++) {
            List<Issue> arrayList = new ArrayList<>();
            List<Issue> linkedList = new LinkedList<>();
            Set<Issue> hashSet = new HashSet<>();
            for (int i = 0; i < SIZE; i++) {
                arrayList.add(new Issue("ISS-" + i, "Задача " + i, i % 13 + 1));
                linkedList.add(new Issue("ISS-" + i, "Задача " + i, i % 13 + 1));
                hashSet.add(new Issue("ISS-" + i, "Задача " + i, i % 13 + 1));
            }
            double insArray = insertMiddle(arrayList);
            double insLinked = insertMiddle(linkedList);
            // Після вставок розмір списків інший, але шукаємо ті самі ключі: вони є в усіх трьох колекціях
            double hasArray = contains(arrayList);
            double hasLinked = contains(linkedList);
            double hasHash = contains(hashSet);
            System.out.printf("%-8d %-14.2f %-14.2f %-14.2f %-14.2f %-14.2f%n", run,
                    insArray, insLinked, hasArray, hasLinked, hasHash);
        }
    }

    private static double insertMiddle(List<Issue> list) {
        long t0 = System.nanoTime();
        for (int i = 0; i < OPS; i++) {
            list.add(list.size() / 2, new Issue("NEW-" + i, "Нова " + i, 3));
        }
        return (System.nanoTime() - t0) / 1_000_000.0;
    }

    private static double contains(java.util.Collection<Issue> collection) {
        int found = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < OPS; i++) {
            // ключі розкидані по всій колекції: від початку до кінця
            if (collection.contains(new Issue("ISS-" + (i * 99), "", 0))) {
                found++;
            }
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        if (found != OPS) {
            throw new IllegalStateException("знайдено " + found + " з " + OPS);
        }
        return ms;
    }
}
