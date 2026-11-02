package ua.duikt.lavrishko.pr4.exp2;

/** Дослід 2: один об'єкт через посилання різних інтерфейсів. */
public class Main {
    public static void main(String[] args) {
        Report report = new Report("Підсумки семестру");

        Printable p = report;
        p.print();                       // через Printable доступні лише його методи
        System.out.println(p.describe());

        Exportable e = report;
        e.export();                      // через Exportable доступні лише його методи
        System.out.println(e.describe());

        System.out.println(report.describe()); // версія з класу: об'єднує обидві
    }
}
