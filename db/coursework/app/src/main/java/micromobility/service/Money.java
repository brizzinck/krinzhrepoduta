package micromobility.service;

import java.math.BigDecimal;

/** Kopecks to hryvnias for messages: 6375 becomes "63,75". */
public final class Money {

    private Money() {
    }

    public static String uah(long kop) {
        return BigDecimal.valueOf(kop, 2).toPlainString().replace('.', ',');
    }
}
