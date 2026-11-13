package ua.duikt.lavrishko.pr6;

/** Дослід 1б: версія на Object. Помилка типу проявляється лише під час виконання. */
public class Exp1Cast {
    public static void main(String[] args) {
        ObjectBox box = new ObjectBox();
        box.put("abc");                    // компілюється без зауважень
        System.out.println("Значення покладено, компіляція пройшла.");
        Issue issue = (Issue) box.get();   // тут ClassCastException
        System.out.println("Прочитано: " + issue);
    }
}
