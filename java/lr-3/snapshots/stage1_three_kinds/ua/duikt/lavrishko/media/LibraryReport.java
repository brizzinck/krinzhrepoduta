package ua.duikt.lavrishko.media;

/**
 * Код обробки колекції. Працює лише з типом MediaContent: жодних перевірок конкретного
 * різновиду й приведень типів, тож новий різновид не потребує змін у цьому класі.
 */
public class LibraryReport {

    public void printAll(MediaContent[] library) {
        for (MediaContent item : library) {
            System.out.println(item.describe());
        }
    }

    public long totalDuration(MediaContent[] library) {
        long total = 0;
        for (MediaContent item : library) {
            total += item.durationSeconds();
        }
        return total;
    }

    public void printSummary(MediaContent[] library) {
        System.out.println("Елементів: " + library.length
                + ", загальна тривалість: " + MediaContent.formatSeconds(totalDuration(library)));
    }
}
