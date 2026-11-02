package ua.duikt.lavrishko.group.app;

import ua.duikt.lavrishko.group.domain.Student;
import ua.duikt.lavrishko.group.domain.StudyGroup;

/** Спроба скористатися службовою операцією узгодження з-поза пакета domain. */
public class CallService {
    public static void main(String[] args) {
        StudyGroup group = new StudyGroup("ТЦР-32");
        Student student = new Student("Іваненко Іван", "KB-001", 85);
        student.attach(group);
    }
}
