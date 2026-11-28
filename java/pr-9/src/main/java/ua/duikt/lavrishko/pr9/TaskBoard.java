package ua.duikt.lavrishko.pr9;

/** Дошка задач: ідентифікатор і кількість задач. Монітором слугує сам об'єкт дошки. */
public class TaskBoard {
    final int id;
    int tasks;

    TaskBoard(int id, int tasks) {
        this.id = id;
        this.tasks = tasks;
    }

    /** Наївне перенесення: бере монітор джерела, чекає 50 мс, потім бере монітор приймача. */
    static void moveTask(TaskBoard from, TaskBoard to) throws InterruptedException {
        synchronized (from) {
            System.out.println(Thread.currentThread().getName() + " тримає дошку " + from.id
                    + ", чекає дошку " + to.id);
            Thread.sleep(50);
            synchronized (to) {
                from.tasks--;
                to.tasks++;
            }
        }
    }

    /** Виправлене перенесення: монітори завжди беруться в порядку зростання id. */
    static void moveTaskOrdered(TaskBoard from, TaskBoard to) throws InterruptedException {
        TaskBoard first = from.id < to.id ? from : to;
        TaskBoard second = first == from ? to : from;
        synchronized (first) {
            Thread.sleep(50);
            synchronized (second) {
                from.tasks--;
                to.tasks++;
            }
        }
    }

    @Override
    public String toString() {
        return "дошка " + id + " (" + tasks + " задач)";
    }
}
