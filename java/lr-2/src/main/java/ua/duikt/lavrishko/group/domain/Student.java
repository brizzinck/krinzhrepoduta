package ua.duikt.lavrishko.group.domain;

/**
 * Студент: елемент агрегата StudyGroup.
 * Незмінні дані (номер, ПІБ, номер залікової книжки) задаються лише в конструкторі,
 * змінюється тільки оцінка, і то через setGrade з перевіркою.
 */
public class Student {

    /** Типові значення для скороченого конструктора. */
    private static final String DEFAULT_RECORD_BOOK = "не вказано";
    private static final int DEFAULT_GRADE = 0;
    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 100;

    /** Спільний лічильник номерів: static, бо належить класу, а не окремому студенту. */
    private static int nextId = 1;

    private final int id;
    private final String fullName;
    private final String recordBookNumber;
    private int grade;
    /** Група, до якої входить студент; встановлюється лише службовими методами пакета. */
    private StudyGroup group;

    /** Повний конструктор: усі дані задаються одразу, некоректні відхиляються. */
    public Student(String fullName, String recordBookNumber, int grade) {
        this.fullName = requireText(fullName, "ПІБ");
        this.recordBookNumber = requireText(recordBookNumber, "номер залікової книжки");
        this.grade = requireGrade(grade);
        // номер присвоюється останнім: відхилений студент не витрачає номер
        this.id = nextId++;
    }

    /** Скорочений конструктор: решта даних отримує типові значення. */
    public Student(String fullName) {
        this(fullName, DEFAULT_RECORD_BOOK, DEFAULT_GRADE);
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRecordBookNumber() {
        return recordBookNumber;
    }

    public int getGrade() {
        return grade;
    }

    /** Контрольована зміна: некоректну оцінку відхиляємо, стан студента лишається попереднім. */
    public void setGrade(int grade) {
        this.grade = requireGrade(grade);
    }

    public boolean isInGroup() {
        return group != null;
    }

    // Службові операції узгодження студента й групи: без модифікатора, тож доступні лише в пакеті domain.

    void attach(StudyGroup group) {
        if (this.group != null) {
            throw new IllegalStateException("студент " + id + " уже входить до групи " + this.group.getName());
        }
        this.group = group;
    }

    void detach() {
        this.group = null;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не може бути порожнім");
        }
        return value.strip();
    }

    private static int requireGrade(int grade) {
        if (grade < MIN_GRADE || grade > MAX_GRADE) {
            throw new IllegalArgumentException(
                    "оцінка " + grade + " поза межами " + MIN_GRADE + ".." + MAX_GRADE);
        }
        return grade;
    }

    @Override
    public String toString() {
        return String.format("#%d %-18s зал. книжка %-12s оцінка %3d", id, fullName, recordBookNumber, grade);
    }
}
