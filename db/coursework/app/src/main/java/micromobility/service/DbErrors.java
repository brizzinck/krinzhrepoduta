package micromobility.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;

/** Turns a database constraint violation into a clear Ukrainian message, by the constraint name in the MySQL error. */
public final class DbErrors {

    private static final Map<String, String> MESSAGES = new LinkedHashMap<>();

    static {
        MESSAGES.put("uq_operators_name", "Оператор з такою назвою вже є.");
        MESSAGES.put("chk_operators_name", "Назва оператора має містити щонайменше 2 символи.");
        MESSAGES.put("chk_operators_phone", "Телефон має вигляд +380XXXXXXXXX.");
        MESSAGES.put("chk_operators_email", "Невірна адреса електронної пошти.");
        MESSAGES.put("chk_operators_commission", "Комісія має бути від 0 до 50 відсотків.");
        MESSAGES.put("fk_tariffs_operator", "Оператора з таким номером не існує.");
        MESSAGES.put("uq_tariffs_operator_type", "Для цього оператора і типу транспорту тариф уже є.");
        MESSAGES.put("chk_tariffs_vehicle_type", "Невірний тип транспорту.");
        MESSAGES.put("chk_tariffs_unlock_fee", "Вартість розблокування має бути від 0 до 100 грн.");
        MESSAGES.put("chk_tariffs_per_minute", "Ціна хвилини має бути від 0,01 до 50 грн.");
        MESSAGES.put("uq_stations_name", "Станція з такою назвою вже є.");
        MESSAGES.put("chk_stations_name", "Назва станції має містити щонайменше 2 символи.");
        MESSAGES.put("chk_stations_capacity", "Місткість станції має бути від 1 до 50.");
        MESSAGES.put("uq_vehicles_code", "Транспорт з таким кодом уже є.");
        MESSAGES.put("chk_vehicles_code", "Код транспорту має вигляд AAA-0000 (великі латинські літери).");
        MESSAGES.put("fk_vehicles_tariff", "У цього оператора немає тарифу для обраного типу транспорту.");
        MESSAGES.put("fk_vehicles_operator", "Оператора з таким номером не існує.");
        MESSAGES.put("fk_vehicles_station", "Станції з таким номером не існує.");
        MESSAGES.put("chk_vehicles_vehicle_type", "Невірний тип транспорту.");
        MESSAGES.put("chk_vehicles_battery", "Заряд має бути від 0 до 100 відсотків.");
        MESSAGES.put("chk_vehicles_status", "Невірний статус транспорту.");
        MESSAGES.put("chk_vehicles_station", "Станція потрібна лише для статусу «Чекає клієнта»; в інших статусах її не вказують.");
        MESSAGES.put("uq_customers_phone", "Клієнт з таким телефоном уже є.");
        MESSAGES.put("uq_customers_email", "Клієнт з такою поштою уже є.");
        MESSAGES.put("chk_customers_names", "Прізвище та імʼя мають містити щонайменше 2 символи.");
        MESSAGES.put("chk_customers_phone", "Телефон має вигляд +380XXXXXXXXX.");
        MESSAGES.put("chk_customers_email", "Невірна адреса електронної пошти.");
        MESSAGES.put("chk_customers_verified", "Час підтвердження не може бути раніше реєстрації.");
        MESSAGES.put("ux_rides_active_customer", "У клієнта вже є активна поїздка.");
        MESSAGES.put("ux_rides_active_vehicle", "Цей транспорт уже в активній поїздці.");
        MESSAGES.put("fk_rides_customer", "Клієнта не можна видалити: у нього є поїздки.");
        MESSAGES.put("fk_rides_vehicle", "Транспорт не можна видалити: за ним є поїздки. Змініть статус на «Списано».");
        MESSAGES.put("chk_rides_period", "Завершення поїздки не може бути раніше початку.");
        MESSAGES.put("chk_rides_commission", "Комісія поїздки не відповідає її вартості.");
        MESSAGES.put("chk_rides_unlock_fee", "Вартість розблокування поїздки поза допустимим діапазоном.");
        MESSAGES.put("chk_rides_per_minute", "Ціна хвилини поїздки поза допустимим діапазоном.");
        MESSAGES.put("fk_payments_ride_customer", "Платіж за поїздку має належати клієнтові цієї поїздки; завершену поїздку видалити не можна.");
        MESSAGES.put("fk_payments_customer", "Клієнта не можна видалити: у нього є платежі.");
        MESSAGES.put("uq_payments_ride_kind", "За цю поїздку такий платіж уже є (одне списання і не більше одного повернення).");
        MESSAGES.put("chk_payments_kind", "Невірний вид платежу.");
        MESSAGES.put("chk_payments_shape", "Сума платежу не відповідає його виду.");
        MESSAGES.put("chk_payments_amount", "Сума поповнення не може перевищувати 10 000 грн.");
    }

    private DbErrors() {
    }

    private static final Pattern[] NAME_PATTERNS = {
            Pattern.compile("Check constraint '([a-z_0-9]+)'"),
            Pattern.compile("for key '[a-z_]+\\.([a-z_0-9]+)'"),
            Pattern.compile("CONSTRAINT `([a-z_0-9]+)`")};

    private static final String GENERAL = "Операцію відхилила база даних: порушено обмеження цілісності.";

    /** The constraint name MySQL reports (CHECK, UNIQUE key or FOREIGN KEY), or null. */
    static String constraintName(String text) {
        for (Pattern p : NAME_PATTERNS) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    /** The Ukrainian message for the violated constraint, else a general one. */
    public static String message(DataIntegrityViolationException e) {
        String text = e.getMostSpecificCause().getMessage();
        String name = text == null ? null : constraintName(text);
        return name == null ? GENERAL : MESSAGES.getOrDefault(name, GENERAL);
    }
}
