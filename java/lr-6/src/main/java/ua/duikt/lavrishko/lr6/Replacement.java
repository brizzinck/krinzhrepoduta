package ua.duikt.lavrishko.lr6;

/** Повтор ключа: що було в сховищі і що прийшло пізніше (воно й лишилось). */
public record Replacement<T>(T before, T after) {
}
