package ua.duikt.lavrishko.group.app;

import ua.duikt.lavrishko.group.domain.Student;

/** Спроба успадкуватися від Student і перевизначити оцінку в обхід перевірок. */
public class Sneaky extends Student {
    public Sneaky() {
        super("Хитрий Студент", "KB-777", 50);
    }

    @Override
    public int getGrade() {
        return 1000;
    }
}
