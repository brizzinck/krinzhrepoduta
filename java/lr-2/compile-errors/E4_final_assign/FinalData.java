/**
 * Те саме поле fullName, що й у Student, але з методом, який намагається його змінити.
 * Усередині класу private не заважає, тож тут видно саме помилку final.
 */
public class FinalData {
    private final String fullName;

    public FinalData(String fullName) {
        this.fullName = fullName;
    }

    public void rename(String newName) {
        this.fullName = newName;
    }
}
