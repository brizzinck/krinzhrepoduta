package ua.duikt.lavrishko.pr9;

/**
 * Дослід 1: втрата оновлень. 16 потоків по 25 000 разів додають 1 до одного лічильника.
 * Запуск: Exp1Race plain (без synchronized) або Exp1Race synced (з synchronized).
 */
public class Exp1Race {
    private static final int THREADS = 16;
    private static final int PER_THREAD = 25_000;

    public static void main(String[] args) throws InterruptedException {
        boolean synced = args.length > 0 && args[0].equals("synced");
        SprintCounter counter = new SprintCounter();
        Runnable step = synced ? counter::closeSynced : counter::close;

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int k = 0; k < PER_THREAD; k++) {
                    step.run();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }

        int expected = THREADS * PER_THREAD;
        int actual = counter.get();
        String mode = synced ? "synchronized" : "без синхронізації";
        System.out.println(mode + ": " + actual + " / " + expected + ", втрачено " + (expected - actual));
    }
}
