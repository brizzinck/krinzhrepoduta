package ua.duikt.lavrishko.media;

/** Аудіокнига: четвертий різновид, доданий без змін у коді обробки. */
public class Audiobook extends MediaContent {

    private final String author;
    private final String narrator;

    public Audiobook(String title, int durationSeconds, String author, String narrator) {
        super(title, durationSeconds);
        this.author = requireText(author, "автор");
        this.narrator = requireText(narrator, "диктор");
    }

    @Override
    public String kind() {
        return "Аудіокнига";
    }

    @Override
    protected String details() {
        return "Автор: " + author + ", читає: " + narrator + ".";
    }
}
