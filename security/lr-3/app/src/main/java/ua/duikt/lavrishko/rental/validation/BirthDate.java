package ua.duikt.lavrishko.rental.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Семантична перевірка дати народження: не з майбутнього, не раніше minYear,
 * вік не менший за minAge (бізнес-правило: оренду авто оформлюють з 18 років).
 */
@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BirthDate {
    int minYear() default 1900;

    int minAge() default 18;

    String message() default "Некоректна дата народження";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
