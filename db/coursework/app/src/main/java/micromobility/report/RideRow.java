package micromobility.report;

import java.time.LocalDateTime;
import micromobility.domain.VehicleType;
import micromobility.service.Billing;

/** R2 row (also the rides list). chargeKop is the RIDE payment (negative), refundKop the REFUND payment. */
public record RideRow(Integer id, String lastName, String firstName, String vehicleCode, String operator,
                      VehicleType vehicleType, LocalDateTime startedAt, LocalDateTime endedAt,
                      Integer chargeKop, Integer refundKop, Integer commissionKop) {

    public String customer() {
        return lastName + " " + firstName;
    }

    public boolean active() {
        return endedAt == null;
    }

    /** Billed minutes; the same code that made the charge. */
    public Integer minutes() {
        return endedAt == null ? null : Billing.minutes(startedAt, endedAt);
    }

    public Integer costKop() {
        return chargeKop == null ? null : -chargeKop;
    }
}
