package ua.duikt.lavrishko.lr6;

/** Задача трекера: ключ, назва, story points. */
public record Issue(String key, String title, int storyPoints) implements Keyed<String> {
    @Override
    public String toString() {
        return key + " " + title + ", " + storyPoints + " SP";
    }
}
