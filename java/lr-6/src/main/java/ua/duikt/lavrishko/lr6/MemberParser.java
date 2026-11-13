package ua.duikt.lavrishko.lr6;

/** Формат рядка: логін;ПІБ;закрито_задач. */
public class MemberParser implements LineParser<Member> {
    @Override
    public Member parse(String line) {
        String[] f = line.split(";", -1);
        if (f.length != 3) {
            throw new IllegalArgumentException("очікувалось 3 поля, отримано " + f.length);
        }
        return new Member(f[0].strip(), f[1].strip(), Integer.parseInt(f[2].strip()));
    }
}
