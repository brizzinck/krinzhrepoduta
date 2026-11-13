package ua.duikt.lavrishko.pr8;

/** Перша залежність: довідник команди. Реалізації немає, у тестах підставляється стаб. */
public interface TeamDirectory {
    /** Скільки story points у виконавця вже в поточному спринті. */
    int currentPoints(String login);
}
