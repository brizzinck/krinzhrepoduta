package rental;

import java.math.BigDecimal;

/** Рядок таблиці cars. */
public record Car(int carId, int modelId, int branchId, String vin, String plateNumber, int manufactureYear,
                  long mileageKm, BigDecimal dailyPrice, String status) {
}
