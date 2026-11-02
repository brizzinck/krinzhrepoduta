package ua.duikt.lavrishko.group.app;

import ua.duikt.lavrishko.group.domain.Student;
import ua.duikt.lavrishko.group.domain.StudyGroup;

import java.util.List;

/** Створює групу, наповнює її, виводить підсумок і пробує порушити стан ззовні. */
public class Main {

    public static void main(String[] args) {
        StudyGroup group = new StudyGroup("ТЦР-32");
        Student ivanov = new Student("Іваненко Іван", "KB-001", 85);
        Student petrenko = new Student("Петренко Петро", "KB-002", 70);
        Student koval = new Student("Коваль Олена", "KB-003", 95);
        Student shortOne = new Student("Мельник Марія");
        group.addStudent(ivanov);
        group.addStudent(petrenko);
        group.addStudent(koval);
        group.addStudent(shortOne);

        System.out.println("Група й студенти:");
        printGroup(group);

        System.out.println("Скорочений конструктор, типові значення:");
        System.out.println(shortOne);

        System.out.println("Спроби порушити стан ззовні:");
        tryModifyList(group);
        tryInvalidStudents();
        tryInvalidGrade(petrenko, group);
        tryDuplicate(group, ivanov);

        System.out.println("Контрольована зміна оцінки:");
        shortOne.setGrade(80);
        System.out.println("Мельник Марія: оцінка була 0, стала 80");
        System.out.println(group.summary());

        System.out.println("Видалення студента:");
        group.removeStudent(petrenko);
        System.out.println(group.summary());
        System.out.println("Студент у групі? " + petrenko.isInGroup());

        System.out.println("Номери студентів:");
        for (Student student : List.of(ivanov, petrenko, koval, shortOne)) {
            System.out.println(student.getFullName() + ": " + student.getId());
        }
        // відхилені студенти (оцінка 105, порожнє ім'я, оцінка -1) номерів не витратили
        Student last = new Student("Останній Студент");
        System.out.println(last.getFullName() + ": " + last.getId() + " (відхилені студенти номерів не витратили)");
    }

    private static void printGroup(StudyGroup group) {
        System.out.println(group.summary());
        for (Student student : group.getStudents()) {
            System.out.println(" + " + student);
        }
    }

    /** getStudents() повертає незмінну копію: ні додати, ні видалити через неї не вдасться. */
    private static void tryModifyList(StudyGroup group) {
        System.out.println("Спроба змінити перелік студентів через getStudents():");
        try {
            group.getStudents().add(new Student("Підсаджений Студент", "KB-999", 100));
            System.out.println("додано (цього бути не повинно)");
        } catch (UnsupportedOperationException e) {
            System.out.println("add(...) дав " + e.getClass().getSimpleName() + ", група не змінилась");
        }
        try {
            group.getStudents().clear();
            System.out.println("очищено (цього бути не повинно)");
        } catch (UnsupportedOperationException e) {
            System.out.println("clear() дав " + e.getClass().getSimpleName() + ", група не змінилась");
        }
        System.out.println(group.summary());
        List<Student> snapshot = group.getStudents();
        Student extra = new Student("Новий Студент", "KB-004", 60);
        group.addStudent(extra);
        System.out.println("копія списку, взята раніше: " + snapshot.size() + " студентів; група тепер: " + group.size());
        group.removeStudent(extra);
        System.out.println("студента повернуто назад, " + group.summary());
    }

    /** Некоректні дані відхиляє конструктор Student: об'єкт навіть не створюється. */
    private static void tryInvalidStudents() {
        System.out.println("Спроба створити студента з некоректними даними:");
        try {
            new Student("Хибний Студент", "KB-100", 105);
        } catch (IllegalArgumentException e) {
            System.out.println("оцінка 105 відхилено: " + e.getMessage());
        }
        try {
            new Student("", "KB-101", 50);
        } catch (IllegalArgumentException e) {
            System.out.println("порожнє ім'я відхилено: " + e.getMessage());
        }
        try {
            new Student("Без Оцінки", "KB-102", -1);
        } catch (IllegalArgumentException e) {
            System.out.println("оцінка -1 відхилено: " + e.getMessage());
        }
    }

    /** Некоректна зміна оцінки відхиляється, оцінка й середній бал лишаються попередніми. */
    private static void tryInvalidGrade(Student student, StudyGroup group) {
        System.out.println("Спроба змінити оцінку на некоректну:");
        int before = student.getGrade();
        try {
            student.setGrade(105);
        } catch (IllegalArgumentException e) {
            System.out.println("setGrade(105) відхилено: " + e.getMessage());
        }
        System.out.println("оцінка лишилась " + student.getGrade() + " (була " + before + "); " + group.summary());
    }

    private static void tryDuplicate(StudyGroup group, Student student) {
        System.out.println("Спроба додати студента, який уже в групі:");
        boolean added = group.addStudent(student);
        System.out.println("addStudent повернув " + added + ", студентів: " + group.size());
        StudyGroup other = new StudyGroup("ТЦР-31");
        System.out.println("додати його до іншої групи: " + other.addStudent(student));
    }
}
