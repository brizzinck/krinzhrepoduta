package ua.duikt.lavrishko.pr4.exp2;

import ua.duikt.lavrishko.pr4.exp1.Document;

/** Звіт: абстрактний клас Document + два інтерфейси. */
public class Report extends Document implements Printable, Exportable {

    public Report(String title) {
        super(title);
    }

    @Override
    public String render() {
        return "ЗВІТ: " + title;
    }

    @Override
    public void print() {
        System.out.println("Друк: " + render());
    }

    @Override
    public void export() {
        System.out.println("Експорт у файл: " + title + ".pdf");
    }

    /** Розв'язання конфлікту default-методів: явний вибір обох версій. */
    @Override
    public String describe() {
        return Printable.super.describe() + "; " + Exportable.super.describe();
    }
}
