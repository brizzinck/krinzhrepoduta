package micromobility.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stores the explicit code of {@link PaymentKind}, not the ordinal and not the name; applied to every PaymentKind field. */
@Converter(autoApply = true)
public class PaymentKindConverter implements AttributeConverter<PaymentKind, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentKind value) {
        return value == null ? null : value.getCode();
    }

    @Override
    public PaymentKind convertToEntityAttribute(Integer code) {
        return code == null ? null : PaymentKind.fromCode(code);
    }
}
