import ua.duikt.lavrishko.pr3.Animal;
import ua.duikt.lavrishko.pr3.Dog;

class NoCast {
    public static void main(String[] args) {
        Animal a = new Dog("Рекс"); // фактично Dog, але тип посилання Animal
        Dog d = a;                  // без явного приведення компілятор не пропускає
    }
}
