package ua.duikt.lavrishko.group.app;

import ua.duikt.lavrishko.group.domain.Student;

/** Спроба змінити унікальний номер студента ззовні. */
public class TamperId {
    public static void main(String[] args) {
        Student student = new Student("Іваненко Іван", "KB-001", 85);
        student.id = 99;
    }
}
