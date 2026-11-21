package micromobility.service;

import micromobility.domain.VehicleStatus;
import micromobility.domain.VehicleType;

/** The fields of the vehicle form. */
public record VehicleData(String code, Integer operatorId, VehicleType vehicleType, String model,
                          Integer batteryPct, VehicleStatus status, Integer stationId) {
}
