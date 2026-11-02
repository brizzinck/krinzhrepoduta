/** final-поле можна задати лише в конструкторі; спроба змінити його в методі. */
public class AccountFinalField {
    private final String id;

    public AccountFinalField(String id) {
        this.id = id;
    }

    public void rename(String newId) {
        this.id = newId;
    }
}
