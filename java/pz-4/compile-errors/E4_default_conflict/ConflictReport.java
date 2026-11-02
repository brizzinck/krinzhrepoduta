import ua.duikt.lavrishko.pr4.exp2.Exportable;
import ua.duikt.lavrishko.pr4.exp2.Printable;

// Обидва інтерфейси мають default describe(); клас його не перевизначає
class ConflictReport implements Printable, Exportable {
    public void print() { }
    public void export() { }
}
