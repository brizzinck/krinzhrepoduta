package ua.duikt.lavrishko.pr4.exp2;

/** Роль «можна експортувати». */
public interface Exportable {

    void export();

    default String describe() {
        return "Exportable: документ можна експортувати";
    }
}
