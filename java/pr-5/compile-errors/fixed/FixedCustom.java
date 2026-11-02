import ua.duikt.lavrishko.pr5.Exp2Cause;
import ua.duikt.lavrishko.pr5.InvalidQuantityException;

/** Виправлений E3: перевірюваний власний виняток обробляється. */
public class FixedCustom {
    public static void main(String[] args) {
        try {
            int quantity = Exp2Cause.parseQuantityWithCause("15");
            System.out.println(quantity);
        } catch (InvalidQuantityException e) {
            System.out.println("Некоректна кількість: " + e.getMessage());
        }
    }
}
