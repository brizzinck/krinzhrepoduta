package micromobility.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stores the explicit code of {@link VehicleType}, not the ordinal and not the name; applied to every VehicleType field. */
@Converter(autoApply = true)
public class VehicleTypeConverter implements AttributeConverter<VehicleType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(VehicleType value) {
        return value == null ? null : value.getCode();
    }

    @Override
    public VehicleType convertToEntityAttribute(Integer code) {
        return code == null ? null : VehicleType.fromCode(code);
    }
}
