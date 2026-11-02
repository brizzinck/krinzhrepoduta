package ua.duikt.lavrishko.pr3;

/** Запускає три досліди практичної роботи (аргумент 1, 2 або 3; без аргументів усі підряд). */
public class Main {

    public static void main(String[] args) {
        String which = args.length > 0 ? args[0] : "all";
        if (which.equals("1") || which.equals("all")) {
            experiment1();
        }
        if (which.equals("2") || which.equals("all")) {
            experiment2();
        }
        if (which.equals("3") || which.equals("all")) {
            experiment3();
        }
    }

    /** Дослід 1: @Override і super, порівняння версій describe(). */
    static void experiment1() {
        System.out.println("Дослід 1: перевизначення і super");
        Animal animal = new Animal("Звір");
        Dog dog = new Dog("Рекс");
        Cat cat = new Cat("Мурка");
        System.out.println("Animal.describe(): " + animal.describe());
        System.out.println("Dog.describe():    " + dog.describe());
        System.out.println("Cat.describe():    " + cat.describe());
    }

    /** Дослід 2: порядок виклику конструкторів при Base b = new Derived(...). */
    static void experiment2() {
        System.out.println("Дослід 2: порядок конструкторів");
        System.out.println("Animal a = new Dog(\"Рекс\");");
        Animal a = new Dog("Рекс");
        System.out.println("Animal b = new Cat(\"Мурка\");");
        Animal b = new Cat("Мурка");
        System.out.println(a.describe());
        System.out.println(b.describe());
    }

    /** Дослід 3: поле за типом посилання, метод за фактичним типом. */
    static void experiment3() {
        System.out.println("Дослід 3: поле і метод через посилання базового типу");
        Animal[] pets = {new Dog("Рекс"), new Cat("Мурка")};
        for (Animal pet : pets) {
            System.out.println("Фактичний тип: " + pet.getClass().getSimpleName());
            System.out.println("pet.label (тип посилання Animal): " + pet.label);
            System.out.println("pet.describe() (метод за фактичним типом): " + pet.describe());
            String derivedLabel = pet instanceof Dog d ? d.label : ((Cat) pet).label;
            System.out.println("поле після приведення до похідного типу: " + derivedLabel);
        }
    }
}
