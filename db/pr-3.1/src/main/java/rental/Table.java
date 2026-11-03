package rental;

import java.util.List;

/** Опис таблиці БД: ім'я, ключові стовпці, чи генерується ключ автоматично, решта стовпців. */
public record Table(String name, List<String> keys, boolean autoKey, List<String> columns) {

    /** Усі 10 таблиць бази car_rental з ПР 1.1. Імена задані тут, користувач вводить лише значення. */
    public static final List<Table> ALL = List.of(
        new Table("branches", List.of("branch_id"), true,
            List.of("name", "city", "address", "phone")),
        new Table("car_classes", List.of("class_id"), true,
            List.of("name", "daily_base_rate", "deposit_amount")),
        new Table("car_models", List.of("model_id"), true,
            List.of("class_id", "brand", "model_name", "seats", "transmission", "fuel_type")),
        new Table("cars", List.of("car_id"), true,
            List.of("model_id", "branch_id", "vin", "plate_number", "manufacture_year", "mileage_km",
                "daily_price", "status")),
        new Table("customers", List.of("customer_id"), true,
            List.of("first_name", "last_name", "middle_name", "phone", "email", "birth_date",
                "license_number", "license_expires", "registered_at")),
        new Table("employees", List.of("employee_id"), true,
            List.of("branch_id", "full_name", "position", "phone", "hired_at")),
        new Table("rentals", List.of("rental_id"), true,
            List.of("customer_id", "car_id", "employee_id", "pickup_branch_id", "return_branch_id", "start_at",
                "planned_end_at", "actual_end_at", "daily_price", "status")),
        new Table("payments", List.of("payment_id"), true,
            List.of("rental_id", "amount", "kind", "method", "paid_at")),
        new Table("extras", List.of("extra_id"), true,
            List.of("name", "price_per_day")),
        new Table("rental_extras", List.of("rental_id", "extra_id"), false,
            List.of("quantity")));

    /** Умова WHERE за ключем для findById, update і delete. */
    public String keyCondition() {
        return String.join(" AND ", keys.stream().map(k -> k + " = ?").toList());
    }
}
