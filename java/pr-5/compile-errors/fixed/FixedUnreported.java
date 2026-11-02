import java.io.FileNotFoundException;
import java.io.FileReader;

/** Виправлений E1: виняток обробляється в try-catch. */
public class FixedUnreported {
    public static void main(String[] args) {
        try {
            new FileReader("data.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Файл не знайдено: " + e.getMessage());
        }
    }
}
