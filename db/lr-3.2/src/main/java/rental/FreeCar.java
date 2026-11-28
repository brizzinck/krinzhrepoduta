package rental;

import java.math.BigDecimal;

/** Результат запиту з фільтрацією: вільне авто в місті. */
public record FreeCar(int carId, String car, String plateNumber, String city, BigDecimal dailyPrice) {
}
