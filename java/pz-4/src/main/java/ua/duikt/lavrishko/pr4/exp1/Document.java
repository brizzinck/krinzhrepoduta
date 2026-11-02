package ua.duikt.lavrishko.pr4.exp1;

/** Абстрактний документ: один абстрактний і один звичайний метод. */
public abstract class Document {

    protected final String title;

    protected Document(String title) {
        this.title = title;
    }

    /** Кожен вид документа формує свій текст. */
    public abstract String render();

    /** Звичайний метод: однаковий для всіх документів. */
    public String info() {
        return "Документ «" + title + "»";
    }
}
