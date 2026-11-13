package ua.duikt.lavrishko.pr6;

import java.util.List;

/** Узагальнені методи для досліду 1. */
public final class Generics {
    private Generics() {
    }

    /** Працює зі списком будь-якого типу; тип результату збігається з типом елементів. */
    public static <T> T first(List<T> items) {
        return items.get(0);
    }

    /**
     * Приймає List<Integer>, List<Double>, List<Number>. З параметром List<Number>
     * список цілих не компілюється (див. compile-errors/E2_list_number).
     */
    public static double total(List<? extends Number> numbers) {
        double sum = 0;
        for (Number n : numbers) {
            sum += n.doubleValue();
        }
        return sum;
    }
}
