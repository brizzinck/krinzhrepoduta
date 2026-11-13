package ua.duikt.lavrishko.pr8;

/** Правило планування спринту: оцінка задачі від 1 до 13, разом у спринті не більше 40 SP. */
public class Sprint {
    public static final int MIN_POINTS = 1;
    public static final int MAX_POINTS = 13;
    public static final int CAPACITY = 40;

    /**
     * Чи можна додати задачу в оцінку points, якщо у спринті вже current SP.
     * Некоректні аргументи: points поза 1..13 і від'ємний current (рішення: так само IllegalArgumentException).
     */
    public boolean canAdd(int points, int current) {
        if (points < MIN_POINTS || points > MAX_POINTS) {
            throw new IllegalArgumentException("оцінка задачі " + points + " поза межами "
                    + MIN_POINTS + ".." + MAX_POINTS);
        }
        if (current < 0) {
            throw new IllegalArgumentException("поточна сума " + current + " не може бути від'ємною");
        }
        return current + points <= CAPACITY;
    }
}
