package micromobility.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import micromobility.domain.Station;

public class StationForm {
    public Integer id;
    @NotBlank(message = "Вкажіть назву")
    @Size(min = 2, max = 60, message = "Назва: від 2 до 60 символів")
    public String name;
    @NotBlank(message = "Вкажіть адресу")
    @Size(max = 100, message = "Адреса: до 100 символів")
    public String address;
    @NotNull(message = "Вкажіть місткість")
    @Min(value = 1, message = "Місткість: від 1 до 50")
    @Max(value = 50, message = "Місткість: від 1 до 50")
    public Integer capacity;

    public static StationForm of(Station s) {
        StationForm f = new StationForm();
        f.id = s.getId();
        f.name = s.getName();
        f.address = s.getAddress();
        f.capacity = s.getCapacity();
        return f;
    }

    public Station toEntity() {
        Station s = new Station();
        s.setName(name);
        s.setAddress(address);
        s.setCapacity(capacity);
        return s;
    }
}
