package ua.duikt.lavrishko.pr5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** Дослід 1: перевірювані й неперевірювані винятки, порядок catch, finally. */
public final class Exp1Checked {

    private Exp1Checked() {
    }

    /** Спосіб 1: виняток обробляється тут, у try-catch. Конкретніший catch стоїть першим. */
    public static void readFirstLineWithCatch(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            System.out.println("Перший рядок: " + reader.readLine());
        } catch (FileNotFoundException e) {
            System.out.println("Файл не знайдено: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Помилка вводу-виводу: " + e.getMessage());
        }
    }

    /** Спосіб 2: виняток оголошено в throws, тож обробка передається викликачу. */
    public static void openWithThrows(String path) throws IOException {
        try (FileReader reader = new FileReader(path)) {
            System.out.println("Файл відкрито: " + path);
        }
    }

    /** Неперевірюваний виняток: ділення цілого на нуль. Компілюється без throws і try-catch. */
    public static int divide(int total, int divisor) {
        return total / divisor;
    }

    /** Неперевірюваний виняток: вихід за межі масиву. */
    public static int elementAt(int[] values, int index) {
        return values[index];
    }

    /** finally виконується і після успіху, і після винятку. */
    public static void readWithFinally(String path) {
        try {
            new FileReader(path).close();
            System.out.println("Файл відкрито й закрито без помилок");
        } catch (IOException e) {
            System.out.println("Спіймано " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            System.out.println("Блок finally виконано");
        }
    }

    /** finally виконується навіть тоді, коли try завершується через return. */
    public static int returnFromTry() {
        try {
            System.out.println("try: готуємо return 1");
            return 1;
        } finally {
            System.out.println("finally: виконується до фактичного виходу з методу");
        }
    }
}
