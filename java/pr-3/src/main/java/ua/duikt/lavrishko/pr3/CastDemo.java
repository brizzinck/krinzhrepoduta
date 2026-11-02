package ua.duikt.lavrishko.pr3;

/** Окремий запуск: приведення вниз для об'єкта, який не є похідним (помилка під час виконання). */
public class CastDemo {
    public static void main(String[] args) {
        Animal real = new Dog("Рекс");
        Dog ok = (Dog) real; // фактичний тип Dog: приведення вдале
        System.out.println("Приведення Animal до Dog для об'єкта Dog: " + ok.describe());

        Animal plain = new Animal("Невідомий");
        try {
            Dog bad = (Dog) plain; // компілюється, але plain не є Dog
            System.out.println(bad);
        } catch (ClassCastException e) {
            System.out.println("Приведення Animal до Dog для об'єкта Animal: " + e.getMessage());
        }
    }
}
