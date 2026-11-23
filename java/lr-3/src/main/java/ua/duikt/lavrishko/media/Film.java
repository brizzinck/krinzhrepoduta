package ua.duikt.lavrishko.media;

/** Фільм: додає режисера та рік виходу. */
public final class Film extends MediaContent {

    public static final int MIN_YEAR = 1888;
    public static final int MAX_YEAR = 2100;

    private final String director;
    private final int year;

    public Film(String title, int durationSeconds, String director, int year) {
        super(title, durationSeconds); // ініціалізація базової частини
        this.director = requireText(director, "режисер");
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("рік виходу " + year + " поза межами " + MIN_YEAR + ".." + MAX_YEAR);
        }
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
