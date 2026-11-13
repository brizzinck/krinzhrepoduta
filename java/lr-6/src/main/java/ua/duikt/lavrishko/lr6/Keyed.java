package ua.duikt.lavrishko.lr6;

/** Запис із ключем. Сховище вимагає від типу записів лише цього. */
public interface Keyed<K> {
    K key();
}
