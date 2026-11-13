package ua.duikt.lavrishko.pr6;

import java.util.ArrayList;
import java.util.List;

/** Дослід 1в: метод із параметром List<? extends Number> приймає обидва списки. */
public class Exp1Total {
    public static void main(String[] args) {
        List<Integer> ints = new ArrayList<>(List.of(5, 8, 13));
        List<Number> numbers = new ArrayList<>(List.of(5, 8.5, 13L));
        System.out.println("total(List<Integer>) = " + Generics.total(ints));
        System.out.println("total(List<Number>)  = " + Generics.total(numbers));

        List<Issue> issues = new ArrayList<>();
        issues.add(new Issue("SHOP-1", "Вхід через Google", 5));
        issues.add(new Issue("SHOP-2", "Експорт у CSV", 8));
        Issue first = Generics.first(issues);   // без приведення типу
        System.out.println("first(List<Issue>) = " + first);
        System.out.println("first(List<Integer>) = " + Generics.first(ints));
    }
}
