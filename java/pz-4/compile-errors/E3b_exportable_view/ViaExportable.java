import ua.duikt.lavrishko.pr4.exp2.Exportable;
import ua.duikt.lavrishko.pr4.exp2.Report;

class ViaExportable {
    public static void main(String[] args) {
        Exportable e = new Report("Назва");
        e.export();
        e.print(); // через посилання Exportable метод print() недоступний
    }
}
