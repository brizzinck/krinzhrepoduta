package ua.duikt.lavrishko.lr7;

/**
 * Задача трекера. Record: equals і hashCode за всіма полями,
 * тому повністю однакові рядки файлу дають рівні об'єкти й distinct() їх усуває.
 */
public record Issue(String key, String status, String assignee, int storyPoints) {

    /** Розбирає рядок формату ключ;статус;виконавець;story_points. */
    public static Issue parse(String line) {
        String[] f = line.split(";", -1);
        if (f.length != 4) {
            throw new IllegalArgumentException("очікувалось 4 поля, отримано " + f.length + ": " + line);
        }
        return new Issue(f[0].strip(), f[1].strip(), f[2].strip(), Integer.parseInt(f[3].strip()));
    }

    @Override
    public String toString() {
        return key + ", " + status + ", виконавець " + assignee + ", " + storyPoints + " SP";
    }
}
