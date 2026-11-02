import ua.duikt.lavrishko.pr4.exp2.Printable;
import ua.duikt.lavrishko.pr4.exp2.Report;

class ViaPrintable {
    public static void main(String[] args) {
        Printable p = new Report("Назва");
        p.print();
        p.export(); // через посилання Printable метод export() недоступний
    }
}
