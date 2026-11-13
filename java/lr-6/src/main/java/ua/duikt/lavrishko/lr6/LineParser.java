package ua.duikt.lavrishko.lr6;

/** Перетворює рядок файлу (без заголовка) на запис. */
public interface LineParser<T> {
    T parse(String line);
}
