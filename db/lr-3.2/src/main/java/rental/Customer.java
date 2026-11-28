package rental;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Рядок таблиці customers. registeredAt заповнює сама БД (DEFAULT CURRENT_TIMESTAMP). */
public record Customer(int customerId, String firstName, String lastName, String middleName, String phone,
                       String email, LocalDate birthDate, String licenseNumber, LocalDate licenseExpires,
                       LocalDateTime registeredAt) {
}
