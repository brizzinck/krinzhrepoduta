package ua.duikt.lavrishko.media;

/** Фільм: додає режисера та рік виходу. */
public class Film extends MediaContent {

    private final String director;
    private final int year;

    public Film(String title, int durationSeconds, String director, int year) {
        super(title, durationSeconds); // ініціалізація базової частини
        this.director = requireText(director, "режисер");
        this.year = year;
    }

    @Override
    public String kind() {
        return "Фільм";
    }

    @Override
    protected String details() {
        return "Режисер: " + director + ", " + year + " р.";
    }
}
