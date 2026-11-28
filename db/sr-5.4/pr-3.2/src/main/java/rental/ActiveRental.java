package rental;

import java.time.LocalDateTime;

/** Результат запиту з об'єднанням: активна оренда з клієнтом, авто й містом видачі. */
public record ActiveRental(int rentalId, String customer, String car, String plateNumber, String pickupCity,
                           LocalDateTime plannedEndAt) {
}
