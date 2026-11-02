import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Навчальний контрприклад: файл читається в пам'ять цілком.
 * Не входить до утиліти, потрібен лише для порівняння з потоковим читанням у -Xmx8m.
 */
public class ReadAllDemo {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
        System.out.println("Прочитано рядків: " + lines.size());
    }
}
