package ua.duikt.lavrishko.pr6;

import java.util.Objects;

/** Задача в трекері. Ключ рівності: поле key. */
public class Issue {
    private String key; // не final: у досліді 3 його змінюють у вже вкладеного в множину об'єкта
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

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    // Рівність і хеш за одним полем: контракт equals/hashCode виконано
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Issue other)) {
            return false;
        }
        return Objects.equals(key, other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return key + " (" + storyPoints + " SP) " + title;
    }
}
