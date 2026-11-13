package ua.duikt.lavrishko.lr6;

import java.util.Comparator;

/** Критерій задач: story points за зростанням, за однакових значень за ключем. */
public class ByStoryPoints implements Comparator<Issue> {
    @Override
    public int compare(Issue a, Issue b) {
        int byPoints = Integer.compare(a.storyPoints(), b.storyPoints());
        return byPoints != 0 ? byPoints : a.key().compareTo(b.key());
    }
}
