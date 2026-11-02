import java.io.FileReader;
import java.io.IOException;

/** Загальний catch (Exception) стоїть перед конкретнішим catch (IOException). */
public class WrongOrder {
    public static void main(String[] args) {
        try {
            new FileReader("data.txt").close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
