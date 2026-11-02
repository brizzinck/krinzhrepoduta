package ua.duikt.lavrishko.pr4.exp1;

/** Лист: реалізує абстрактний метод render() по-своєму. */
public class Letter extends Document {

    private final String addressee;

    public Letter(String title, String addressee) {
        super(title);
        this.addressee = addressee;
    }

    @Override
    public String render() {
        return "Шановний(а) " + addressee + "! Тема листа: " + title;
    }
}
