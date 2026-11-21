package micromobility.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import micromobility.domain.Customer;

public class CustomerForm {
    public Integer id;
    @NotBlank(message = "Вкажіть прізвище")
    @Size(min = 2, max = 40, message = "Прізвище: від 2 до 40 символів")
    public String lastName;
    @NotBlank(message = "Вкажіть імʼя")
    @Size(min = 2, max = 40, message = "Імʼя: від 2 до 40 символів")
    public String firstName;
    @NotBlank(message = "Вкажіть телефон")
    @Pattern(regexp = "^\\+380[0-9]{9}$", message = "Телефон має вигляд +380XXXXXXXXX")
    public String phone;
    @Size(max = 100, message = "E-mail: до 100 символів")
    @Pattern(regexp = "^.+@.+\\..+$", message = "Невірний e-mail")
    public String email;

    public static CustomerForm of(Customer c) {
        CustomerForm f = new CustomerForm();
        f.id = c.getId();
        f.lastName = c.getLastName();
        f.firstName = c.getFirstName();
        f.phone = c.getPhone();
        f.email = c.getEmail();
        return f;
    }
}
