package micromobility.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stores the explicit code of {@link VehicleStatus}, not the ordinal and not the name; applied to every VehicleStatus field. */
@Converter(autoApply = true)
public class VehicleStatusConverter implements AttributeConverter<VehicleStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(VehicleStatus value) {
        return value == null ? null : value.getCode();
    }

    @Override
    public VehicleStatus convertToEntityAttribute(Integer code) {
        return code == null ? null : VehicleStatus.fromCode(code);
    }
}
