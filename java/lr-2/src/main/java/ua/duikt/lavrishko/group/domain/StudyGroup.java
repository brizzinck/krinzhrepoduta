package ua.duikt.lavrishko.group.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Навчальна група: агрегат, що зберігає студентів і рахує середній бал.
 * Внутрішній список недоступний ззовні, а середній бал обчислюється за поточним набором студентів.
 */
public class StudyGroup {

    private final String name;
    private final List<Student> students = new ArrayList<>();

    public StudyGroup(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("назва групи не може бути порожньою");
        }
        this.name = name.strip();
    }

    public String getName() {
        return name;
    }

    /**
     * Додає студента. Повертає false, якщо він уже входить до якоїсь групи (в цій чи іншій):
     * студент належить не більш як одній групі.
     */
    public boolean addStudent(Student student) {
        Objects.requireNonNull(student, "student");
        if (student.isInGroup()) {
            return false;
        }
        students.add(student);
        student.attach(this);
        return true;
    }

    /** Видаляє студента з групи; повертає false, якщо його там не було. */
    public boolean removeStudent(Student student) {
        if (students.remove(student)) {
            student.detach();
            return true;
        }
        return false;
    }

    public int size() {
        return students.size();
    }

    /** Незмінна копія списку: змінити склад групи через неї неможливо. */
    public List<Student> getStudents() {
        return List.copyOf(students);
    }

    /**
     * Середній бал за поточними оцінками студентів. Не зберігається в полі, а обчислюється щоразу,
     * тому завжди відповідає набору. Для порожньої групи повертає 0.0.
     */
    public double averageGrade() {
        if (students.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (Student student : students) {
            sum += student.getGrade();
        }
        return (double) sum / students.size();
    }

    public String summary() {
        return String.format(Locale.ROOT, "Група %s, студентів: %d, середній бал: %.2f",
                name, students.size(), averageGrade());
    }
}
