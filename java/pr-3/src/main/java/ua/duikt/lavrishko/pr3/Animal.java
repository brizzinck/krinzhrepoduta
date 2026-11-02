package ua.duikt.lavrishko.pr3;

/** Базовий клас варіанта 2: Тварина. Конструктора без параметрів немає навмисно. */
public class Animal {

    /** Поле з тим самим іменем є й у похідних класах (приховування поля). */
    public String label = "базове";

    protected final String name;

    public Animal(String name) {
        this.name = name;
        System.out.println("[Animal] конструктор, ім'я: " + name);
    }

    public String describe() {
        return "Тварина " + name;
    }
}
