package micromobility.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import micromobility.domain.Vehicle;
import micromobility.domain.VehicleStatus;
import micromobility.domain.VehicleType;
import micromobility.service.VehicleData;

public class VehicleForm {
    public Integer id;
    @NotBlank(message = "Вкажіть код")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{4}$", message = "Код має вигляд AAA-0000 (великі латинські літери)")
    public String code;
    @NotNull(message = "Оберіть оператора")
    public Integer operatorId;
    @NotNull(message = "Оберіть тип")
    public VehicleType vehicleType;
    @NotBlank(message = "Вкажіть модель")
    @Size(max = 40, message = "Модель: до 40 символів")
    public String model;
    @NotNull(message = "Вкажіть заряд")
    @Min(value = 0, message = "Заряд: від 0 до 100")
    @Max(value = 100, message = "Заряд: від 0 до 100")
    public Integer batteryPct;
    @NotNull(message = "Оберіть статус")
    public VehicleStatus status = VehicleStatus.AVAILABLE;
    public Integer stationId;

    public static VehicleForm of(Vehicle v) {
        VehicleForm f = new VehicleForm();
        f.id = v.getId();
        f.code = v.getCode();
        f.operatorId = v.getOperator().getId();
        f.vehicleType = v.getVehicleType();
        f.model = v.getModel();
        f.batteryPct = v.getBatteryPct();
        f.status = v.getStatus();
        f.stationId = v.getStation() == null ? null : v.getStation().getId();
        return f;
    }

    public VehicleData toData() {
        return new VehicleData(code, operatorId, vehicleType, model, batteryPct, status, stationId);
    }
}
