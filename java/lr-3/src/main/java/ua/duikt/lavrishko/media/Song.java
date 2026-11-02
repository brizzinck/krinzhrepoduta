package ua.duikt.lavrishko.media;

/** Пісня: додає виконавця й альбом. */
public class Song extends MediaContent {

    private final String artist;
    private final String album;

    public Song(String title, int durationSeconds, String artist, String album) {
        super(title, durationSeconds);
        this.artist = requireText(artist, "виконавець");
        this.album = requireText(album, "альбом");
    }

    @Override
    public String kind() {
        return "Пісня";
    }

    @Override
    protected String details() {
        return "Виконавець: " + artist + ", альбом: " + album + ".";
    }
}
