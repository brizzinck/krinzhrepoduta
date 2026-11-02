package ua.duikt.lavrishko.rental.repo;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CarRepository {
    private final JdbcTemplate jdbc;

    public CarRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Безпечний пошук: значення brand передається як параметр «?», а не вклеюється в текст запиту. */
    public List<Map<String, Object>> searchByBrand(String brand) {
        return jdbc.queryForList(
                "SELECT brand, model, production_year, daily_price FROM cars WHERE brand = ? ORDER BY id", brand);
    }
}
