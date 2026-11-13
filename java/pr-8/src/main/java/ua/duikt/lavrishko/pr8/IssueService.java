package ua.duikt.lavrishko.pr8;

/** Планування задачі: бере поточне навантаження з довідника, застосовує правило, повідомляє в чат. */
public class IssueService {
    private final TeamDirectory directory;
    private final ChatNotifier notifier;
    private final Sprint sprint = new Sprint();

    public IssueService(TeamDirectory directory, ChatNotifier notifier) {
        this.directory = directory;
        this.notifier = notifier;
    }

    /** Повертає true, якщо задачу на points SP можна додати виконавцю login. */
    public boolean plan(String login, int points) {
        int current = directory.currentPoints(login);
        boolean added = sprint.canAdd(points, current);
        String text = added
                ? "Задачу на " + points + " SP додано в спринт"
                : "Задача на " + points + " SP не вміщується в спринт (зараз " + current + " SP)";
        notifier.send(login, text);
        return added;
    }
}
