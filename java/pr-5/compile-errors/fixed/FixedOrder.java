import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** Виправлений E2: конкретніші винятки йдуть перед загальнішими. */
public class FixedOrder {
    public static void main(String[] args) {
        try {
            new FileReader("data.txt").close();
        } catch (FileNotFoundException e) {
            System.out.println("Файл не знайдено: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
