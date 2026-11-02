package ua.duikt.lavrishko.pr4.exp1;

/** Дослід 1: робочий варіант після виправлення помилок компіляції. */
public class Main {
    public static void main(String[] args) {
        Document[] documents = {
                new Report("Підсумки семестру"),
                new Letter("Запрошення на захист", "Ніщеменко Д. О.")
        };
        for (Document d : documents) {
            System.out.println(d.info());   // звичайний метод базового класу
            System.out.println(d.render()); // абстрактний метод, реалізований у похідних
        }
    }
}
