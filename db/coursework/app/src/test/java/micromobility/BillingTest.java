package micromobility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import micromobility.service.Billing;
import org.junit.jupiter.api.Test;

class BillingTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 1, 10, 0, 0);

    @Test
    void zeroSecondsAreBilledAsOneMinute() {
        assertEquals(1, Billing.minutes(START, START));
    }

    @Test
    void fiftyNineSecondsAreOneMinute() {
        assertEquals(1, Billing.minutes(START, START.plusSeconds(59)));
    }

    @Test
    void sixtySecondsAreOneMinute() {
        assertEquals(1, Billing.minutes(START, START.plusSeconds(60)));
    }

    @Test
    void sixtyOneSecondsAreTwoMinutes() {
        assertEquals(2, Billing.minutes(START, START.plusSeconds(61)));
    }

    @Test
    void fiftyFiveMinutesTwelveSecondsAreFiftySixMinutes() {
        assertEquals(56, Billing.minutes(START, START.plusMinutes(55).plusSeconds(12)));
    }

    @Test
    void priceIsUnlockFeePlusRateTimesMinutes() {
        // Bolt scooter: 10.00 UAH + 5.20 UAH per minute; 10 minutes = 62.00 UAH
        assertEquals(6200, Billing.price(1000, 520, 10));
    }

    @Test
    void commissionRoundsHalfUp() {
        assertEquals(638, Billing.commission(6375, new BigDecimal("10.00")));
    }

    @Test
    void zeroPercentCommissionIsZero() {
        assertEquals(0, Billing.commission(6375, new BigDecimal("0.00")));
    }

    @Test
    void fiftyPercentCommissionIsHalfThePrice() {
        assertEquals(3188, Billing.commission(6375, new BigDecimal("50.00")));
    }
}
