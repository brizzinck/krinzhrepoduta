import ua.duikt.lavrishko.pr5.Exp2Cause;

/** Метод оголошує власний перевірюваний виняток, а викликальний код його не обробляє. */
public class UnreportedCustom {
    public static void main(String[] args) {
        int quantity = Exp2Cause.parseQuantityWithCause("15");
        System.out.println(quantity);
    }
}
