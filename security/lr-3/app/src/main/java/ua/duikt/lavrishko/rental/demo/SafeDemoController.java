package ua.duikt.lavrishko.rental.demo;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.duikt.lavrishko.rental.repo.CarRepository;

/** Захищені двійники вразливих сторінок: параметризований запит, екранування виводу, типізовані параметри. */
@Controller
@RequestMapping("/demo/safe")
public class SafeDemoController {
    private final CarRepository cars;

    public SafeDemoController(CarRepository cars) {
        this.cars = cars;
    }

    /** Пошук за брендом: довжина до 64 символів (400 при перевищенні), SQL параметризований, вивід через th:text. */
    @GetMapping("/search")
    public String search(@RequestParam @Size(max = 64) String brand, Model model) {
        List<Map<String, Object>> rows = cars.searchByBrand(brand);
        model.addAttribute("brand", brand);
        model.addAttribute("rows", rows);
        model.addAttribute("mode", "safe");
        return "search-safe";
    }

    /** Параметр int: число поза діапазоном int чи не число дає 400 (обробляє ApiExceptionHandler), а не 500. */
    @GetMapping("/page")
    @ResponseBody
    public Map<String, Object> page(@RequestParam int n) {
        return Map.of("page", n);
    }
}
