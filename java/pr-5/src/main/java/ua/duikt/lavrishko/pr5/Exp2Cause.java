package ua.duikt.lavrishko.pr5;

/** Дослід 2: власний виняток, перекидання з причиною й без неї. */
public final class Exp2Cause {

    private Exp2Cause() {
    }

    /** Спосіб 1: у новий виняток переноситься лише текст повідомлення. */
    public static int parseQuantityWithoutCause(String text) throws InvalidQuantityException {
        try {
            return checkPositive(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException(e.getMessage());
        }
    }

    /** Спосіб 2: початковий виняток передається другим аргументом і лишається в ланцюжку. */
    public static int parseQuantityWithCause(String text) throws InvalidQuantityException {
        try {
            return checkPositive(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException("Некоректна кількість", e);
        }
    }

    private static int checkPositive(int quantity) throws InvalidQuantityException {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Кількість має бути більше 0: " + quantity);
        }
        return quantity;
    }
}
