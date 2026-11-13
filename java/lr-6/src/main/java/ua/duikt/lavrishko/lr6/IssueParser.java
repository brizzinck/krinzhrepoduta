package ua.duikt.lavrishko.lr6;

/** Формат рядка: ключ;назва;story_points. */
public class IssueParser implements LineParser<Issue> {
    @Override
    public Issue parse(String line) {
        String[] f = line.split(";", -1);
        if (f.length != 3) {
            throw new IllegalArgumentException("очікувалось 3 поля, отримано " + f.length);
        }
        return new Issue(f[0].strip(), f[1].strip(), Integer.parseInt(f[2].strip()));
    }
}
