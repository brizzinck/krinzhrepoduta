package micromobility.report;

import micromobility.domain.VehicleType;

/** R1 row. Components have the exact types of the JPQL expressions (the constructor is chosen by them). */
public record AvailableVehicleRow(Integer id, String code, String station, String operator, VehicleType vehicleType,
                                  String model, Integer batteryPct, Integer unlockFeeKop, Integer perMinuteKop) {
    public int price10MinKop() {
        return unlockFeeKop + 10 * perMinuteKop;
    }
}
