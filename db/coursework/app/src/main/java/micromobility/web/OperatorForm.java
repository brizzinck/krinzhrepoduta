package micromobility.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import micromobility.domain.Operator;

/** Validation mirrors the CHECKs of operators. */
public class OperatorForm {
    public Integer id;
    @NotBlank(message = "Вкажіть назву")
    @Size(min = 2, max = 40, message = "Назва: від 2 до 40 символів")
    public String name;
    @NotBlank(message = "Вкажіть телефон")
    @Pattern(regexp = "^\\+380[0-9]{9}$", message = "Телефон має вигляд +380XXXXXXXXX")
    public String phone;
    @NotBlank(message = "Вкажіть e-mail")
    @Pattern(regexp = "^.+@.+\\..+$", message = "Невірний e-mail")
    @Size(max = 100, message = "E-mail: до 100 символів")
    public String email;
    @NotNull(message = "Вкажіть комісію")
    @DecimalMin(value = "0", message = "Комісія: від 0 до 50 %")
    @DecimalMax(value = "50", message = "Комісія: від 0 до 50 %")
    @Digits(integer = 2, fraction = 2, message = "Комісія: не більше двох знаків після коми")
    public BigDecimal commissionPct = new BigDecimal("10.00");

    public static OperatorForm of(Operator o) {
        OperatorForm f = new OperatorForm();
        f.id = o.getId();
        f.name = o.getName();
        f.phone = o.getPhone();
        f.email = o.getEmail();
        f.commissionPct = o.getCommissionPct();
        return f;
    }

    public Operator toEntity() {
        Operator o = new Operator();
        o.setName(name);
        o.setPhone(phone);
        o.setEmail(email);
        o.setCommissionPct(commissionPct);
        return o;
    }
}
