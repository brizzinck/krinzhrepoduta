package ua.duikt.lavrishko.pr4.exp2;

/** Роль «можна друкувати». */
public interface Printable {

    void print();

    /** Метод за замовчуванням: така сама сигнатура є й в Exportable. */
    default String describe() {
        return "Printable: документ можна надрукувати";
    }
}
