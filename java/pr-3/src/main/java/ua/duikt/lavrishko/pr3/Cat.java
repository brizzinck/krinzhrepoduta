package ua.duikt.lavrishko.pr3;

/** Похідний клас: Кіт. */
public class Cat extends Animal {

    /** Приховує поле Animal.label. */
    public String label = "похідне";

    public Cat(String name) {
        super(name);
        System.out.println("[Cat] конструктор");
    }

    @Override
    public String describe() {
        return super.describe() + ". Кіт " + name + " нявкає";
    }
}
