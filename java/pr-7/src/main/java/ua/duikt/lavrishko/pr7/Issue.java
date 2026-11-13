package ua.duikt.lavrishko.pr7;

/** Задача в трекері: ключ, назва, story points. Без equals і hashCode, вони в цій роботі не потрібні. */
public class Issue {
    private final String key;
    private final String title;
    private final int storyPoints;

    public Issue(String key, String title, int storyPoints) {
        this.key = key;
        this.title = title;
        this.storyPoints = storyPoints;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    @Override
    public String toString() {
        return key + " (" + storyPoints + " SP) " + title;
    }
}
