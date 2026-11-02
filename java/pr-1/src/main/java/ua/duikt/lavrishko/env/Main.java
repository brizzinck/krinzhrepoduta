package ua.duikt.lavrishko.env;

import org.apache.commons.lang3.StringUtils;

/** Перевірка середовища: версія JVM, ОС, метод із залежності та український рядок. */
public class Main {

    public static void main(String[] args) {
        System.out.println("JVM: " + Runtime.version() + " (" + System.getProperty("java.vendor") + ")");
        System.out.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        // Метод із підключеної залежності commons-lang3
        System.out.println("Reversed (Commons Lang): " + StringUtils.reverse("Java"));
        greet();
    }

    /** Окремий метод: його байт-код аналізуємо через javap -c. */
    public static void greet() {
        System.out.println("Привіт зі збірки!");
    }
}
