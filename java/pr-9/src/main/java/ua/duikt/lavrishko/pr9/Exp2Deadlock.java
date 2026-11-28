package ua.duikt.lavrishko.pr9;

/**
 * Дослід 2: взаємне блокування. Два потоки переносять задачу між двома дошками у протилежних напрямках.
 * Запуск: Exp2Deadlock naive (зависає) або Exp2Deadlock ordered (монітори за зростанням id, завершується).
 */
public class Exp2Deadlock {
    @FunctionalInterface
    interface Move {
        void run(TaskBoard from, TaskBoard to) throws InterruptedException;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean ordered = args.length > 0 && args[0].equals("ordered");
        Move move = ordered ? TaskBoard::moveTaskOrdered : TaskBoard::moveTask;

        TaskBoard board1 = new TaskBoard(1, 10);
        TaskBoard board2 = new TaskBoard(2, 10);

        Thread t1 = new Thread(() -> run(move, board1, board2));
        Thread t2 = new Thread(() -> run(move, board2, board1));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Дошка 1: " + board1.tasks + " задач, дошка 2: " + board2.tasks
                + " задач, разом " + (board1.tasks + board2.tasks) + ". Програма завершилась.");
    }

    private static void run(Move move, TaskBoard from, TaskBoard to) {
        try {
            move.run(from, to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
