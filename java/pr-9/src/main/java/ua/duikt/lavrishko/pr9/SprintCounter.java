package ua.duikt.lavrishko.pr9;

/** Лічильник закритих задач спринту: одне поле int і два методи, що додають 1. */
public class SprintCounter {
    private int closed;

    /** Без синхронізації: closed++ це читання, додавання і запис, потоки можуть перебити один одного. */
    public void close() {
        closed++;
    }

    /** Той самий код, але під монітором: одночасно в методі лише один потік. */
    public synchronized void closeSynced() {
        closed++;
    }

    public int get() {
        return closed;
    }
}
