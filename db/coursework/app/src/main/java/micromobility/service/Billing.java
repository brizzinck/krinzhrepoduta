package micromobility.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Ride billing in one place (BR10). Money is integer kopecks.
 * minutes = max(1, ceil(seconds / 60)); price = unlock fee + rate * minutes; commission = price * pct / 100, HALF_UP.
 */
public final class Billing {

    private Billing() {
    }

    public static int minutes(LocalDateTime startedAt, LocalDateTime endedAt) {
        long seconds = Duration.between(startedAt, endedAt).getSeconds();
        long minutes = (seconds + 59) / 60;
        return (int) Math.max(1, minutes);
    }

    public static int price(int unlockFeeKop, int perMinuteKop, int minutes) {
        return Math.toIntExact(unlockFeeKop + (long) perMinuteKop * minutes);
    }

    public static int commission(int priceKop, BigDecimal commissionPct) {
        return BigDecimal.valueOf(priceKop).multiply(commissionPct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).intValueExact();
    }
}
