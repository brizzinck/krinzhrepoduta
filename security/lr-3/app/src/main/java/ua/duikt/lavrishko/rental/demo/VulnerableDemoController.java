package ua.duikt.lavrishko.rental.demo;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * Навмисно вразливий код для навчальної демонстрації атак, у реальних проєктах не використовувати.
 * Компонент створюється лише з профілем Spring «vuln» (SPRING_PROFILES_ACTIVE=vuln) і потрібен
 * для порівняння «до захисту / після захисту». Основний шлях реєстрації (/register) його не використовує.
 */
@Profile("vuln")
@Controller
@RequestMapping("/demo/vuln")
public class VulnerableDemoController {
    private final JdbcTemplate jdbc;

    public VulnerableDemoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** ПОМИЛКА 1: SQL будується конкатенацією. ПОМИЛКА 2: у шаблоні вивід через th:utext (без екранування). */
    @GetMapping("/search")
    public String search(@RequestParam String brand, Model model) {
        String sql = "SELECT brand, model, production_year, daily_price FROM cars WHERE brand = '" + brand + "' ORDER BY id";
        model.addAttribute("sql", sql);
        model.addAttribute("brand", brand);
        model.addAttribute("rows", jdbc.queryForList(sql));
        model.addAttribute("mode", "vuln");
        return "search-vuln";
    }

    /** ПОМИЛКА 3: ручний parseInt без обробки винятку, відповідь 500 зі службовими подробицями. */
    @GetMapping("/page")
    @ResponseBody
    public Map<String, Object> page(@RequestParam String n) {
        return Map.of("page", Integer.parseInt(n));
    }

    /** ПОМИЛКА 4: реєстрація без валідації, SQL конкатенацією, пароль зберігається як є. */
    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestParam String login, @RequestParam String firstName,
                           @RequestParam String email, @RequestParam String password,
                           @RequestParam String phone, @RequestParam String birthDate) {
        String sql = "INSERT INTO users (login, first_name, email, password_hash, phone, birth_date) VALUES ('"
                + login + "','" + firstName + "','" + email + "','" + password + "','" + phone + "','"
                + birthDate + "')";
        jdbc.update(sql);
        return "Користувача створено: " + login;
    }

    /** ПОМИЛКА 5: імена з БД виводяться без екранування (збережений XSS). */
    @GetMapping("/users")
    public String users(Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT login, first_name, email FROM users ORDER BY id");
        model.addAttribute("rows", rows);
        return "users-vuln";
    }
}
