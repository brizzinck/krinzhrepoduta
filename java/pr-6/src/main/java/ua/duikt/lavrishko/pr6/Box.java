package ua.duikt.lavrishko.pr6;

/** Узагальнений контейнер для одного значення типу T. */
public class Box<T> {
    private T value;

    public void put(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
