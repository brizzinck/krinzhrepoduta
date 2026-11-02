import ua.duikt.lavrishko.pr3.Animal;

// Те саме, що BadDog, але з явним super(name): компілюється
class GoodDog extends Animal {
    GoodDog(String name) {
        super(name);
        System.out.println("[GoodDog] конструктор");
    }
}
