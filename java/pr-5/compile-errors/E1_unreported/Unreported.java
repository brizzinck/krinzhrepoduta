import java.io.FileReader;

/** Перевірюваний виняток без обробки: компілятор має відмовити. */
public class Unreported {
    public static void main(String[] args) {
        new FileReader("data.txt");
    }
}
