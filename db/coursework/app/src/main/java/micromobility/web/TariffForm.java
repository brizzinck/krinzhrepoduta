package micromobility.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import micromobility.domain.Tariff;
import micromobility.domain.VehicleType;

/** Prices are typed in hryvnias and stored in kopecks. */
public class TariffForm {
    public Integer id;
    @NotNull(message = "Оберіть оператора")
    public Integer operatorId;
    @NotNull(message = "Оберіть тип транспорту")
    public VehicleType vehicleType;
    @NotNull(message = "Вкажіть вартість розблокування")
    @DecimalMin(value = "0", message = "Розблокування: від 0 до 100 грн")
    @DecimalMax(value = "100", message = "Розблокування: від 0 до 100 грн")
    @Digits(integer = 3, fraction = 2, message = "Не більше двох знаків після коми")
    public BigDecimal unlockFee;
    @NotNull(message = "Вкажіть ціну хвилини")
    @DecimalMin(value = "0.01", message = "Хвилина: від 0,01 до 50 грн")
    @DecimalMax(value = "50", message = "Хвилина: від 0,01 до 50 грн")
    @Digits(integer = 2, fraction = 2, message = "Не більше двох знаків після коми")
    public BigDecimal perMinute;

    public static TariffForm of(Tariff t) {
        TariffForm f = new TariffForm();
        f.id = t.getId();
        f.operatorId = t.getOperator().getId();
        f.vehicleType = t.getVehicleType();
        f.unlockFee = BigDecimal.valueOf(t.getUnlockFeeKop(), 2);
        f.perMinute = BigDecimal.valueOf(t.getPerMinuteKop(), 2);
        return f;
    }

    public int unlockFeeKop() {
        return unlockFee.movePointRight(2).intValueExact();
    }

    public int perMinuteKop() {
        return perMinute.movePointRight(2).intValueExact();
    }
}
