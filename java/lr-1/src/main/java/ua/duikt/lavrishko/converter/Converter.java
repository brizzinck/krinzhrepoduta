package ua.duikt.lavrishko.converter;

import java.math.BigDecimal;
import java.math.MathContext;

/** Перетворення площі через базову одиницю m²: достатньо одного коефіцієнта на одиницю. */
public final class Converter {

    private static final MathContext PRECISION = new MathContext(12);

    private Converter() {
    }

    public static double convert(double value, AreaUnit from, AreaUnit to) {
        return value * from.squareMetres() / to.squareMetres();
    }

    /** Текст результату: 12 значущих цифр, щоб не показувати похибку double. Значення має бути скінченним. */
    public static String format(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).round(PRECISION).stripTrailingZeros();
        double abs = Math.abs(value);
        if (value != 0 && (abs >= 1e15 || abs < 1e-6)) {
            return rounded.toString();
        }
        return rounded.toPlainString();
    }
}
