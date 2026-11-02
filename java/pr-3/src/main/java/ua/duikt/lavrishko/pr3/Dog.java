package ua.duikt.lavrishko.pr3;

/** Похідний клас: Собака. */
public class Dog extends Animal {

    /** Приховує поле Animal.label: у об'єкта тепер два окремі поля з однаковим іменем. */
    public String label = "похідне";

    public Dog(String name) {
        super(name); // має бути першим оператором конструктора
        System.out.println("[Dog] конструктор");
    }

    @Override
    public String describe() {
        // Спершу викликаємо батьківську версію, потім доповнюємо її
        return super.describe() + ". Собака " + name + " гавкає";
    }
}
