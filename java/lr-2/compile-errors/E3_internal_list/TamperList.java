package ua.duikt.lavrishko.group.app;

import ua.duikt.lavrishko.group.domain.Student;
import ua.duikt.lavrishko.group.domain.StudyGroup;

import java.util.ArrayList;

/** Спроба напряму дістатися до внутрішнього списку студентів групи. */
public class TamperList {
    public static void main(String[] args) {
        StudyGroup group = new StudyGroup("ТЦР-32");
        group.students.add(new Student("Підсаджений Студент", "KB-999", 100));
        group.students = new ArrayList<>();
    }
}
