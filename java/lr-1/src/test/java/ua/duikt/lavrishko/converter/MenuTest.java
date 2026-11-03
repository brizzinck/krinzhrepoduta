package ua.duikt.lavrishko.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuTest {

    private static String run(String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new Menu(new Scanner(input).useLocale(Locale.ROOT), out).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void oneBadValueGivesOneMessage() {
        String output = run("1\n1\nha\n2\nabc\n0\n");
        assertTrue(output.contains("1 ha = 10000 m²"));
        assertEquals(2, output.split("введіть число", -1).length);
    }

    @Test
    void endOfInputFinishesWithoutException() {
        assertTrue(run("1\n5\n").endsWith("Вихід. Дякуємо за використання!" + System.lineSeparator()));
    }
}
