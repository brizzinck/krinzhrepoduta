package ua.duikt.lavrishko.pr4.exp1;

/** Звіт: реалізує абстрактний метод render(). */
public class Report extends Document {

    public Report(String title) {
        super(title);
    }

    @Override
    public String render() {
        return "ЗВІТ: " + title + " (розділи, таблиці, висновки)";
    }
}
