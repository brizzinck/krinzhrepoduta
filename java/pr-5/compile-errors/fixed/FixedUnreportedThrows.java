import java.io.FileNotFoundException;
import java.io.FileReader;

/** Виправлений E1, другий спосіб: виняток оголошено в throws. */
public class FixedUnreportedThrows {
    public static void main(String[] args) throws FileNotFoundException {
        new FileReader("data.txt");
    }
}
