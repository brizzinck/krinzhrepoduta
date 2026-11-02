import ua.duikt.lavrishko.pr3.Animal;

// Конструктор є, але виклику super(...) немає, а в Animal немає конструктора без параметрів
class BadDog extends Animal {
    BadDog(String name) {
        System.out.println("[BadDog] конструктор");
    }
}
