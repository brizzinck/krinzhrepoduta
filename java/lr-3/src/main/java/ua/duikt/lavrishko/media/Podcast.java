package ua.duikt.lavrishko.media;

/** Подкаст: додає ведучого та номер випуску. */
public final class Podcast extends MediaContent {

    private final String host;
    private final int episode;

    public Podcast(String title, int durationSeconds, String host, int episode) {
        super(title, durationSeconds);
        this.host = requireText(host, "ведучий");
        if (episode <= 0) {
            throw new IllegalArgumentException("номер випуску має бути більшим за 0, отримано " + episode);
        }
        this.episode = episode;
    }

    @Override
    public String kind() {
        return "Подкаст";
    }

    @Override
    protected String details() {
        return "Ведучий: " + host + ", випуск № " + episode + ".";
    }
}
