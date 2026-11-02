package ua.duikt.lavrishko.rental.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.LocalDate;

public class BirthDateValidator implements ConstraintValidator<BirthDate, LocalDate> {
    private final Clock clock;
    private int minYear;
    private int minAge;

    public BirthDateValidator() {
        this(Clock.systemDefaultZone());
    }

    BirthDateValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void initialize(BirthDate annotation) {
        this.minYear = annotation.minYear();
        this.minAge = annotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // порожнє значення відхиляє @NotNull
        }
        LocalDate today = LocalDate.now(clock);
        if (value.isAfter(today)) {
            return fail(context, "Дата народження не може бути в майбутньому");
        }
        if (value.getYear() < minYear) {
            return fail(context, "Дата народження не може бути раніше " + minYear + " року");
        }
        if (value.isAfter(today.minusYears(minAge))) {
            return fail(context, "Орендувати авто можна з " + minAge + " років");
        }
        return true;
    }

    private static boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(escape(message)).addConstraintViolation();
        return false;
    }

    /** Символи «{», «}», «$» мають особливе значення в шаблонах повідомлень Bean Validation. */
    private static String escape(String message) {
        return message.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("$", "\\$");
    }
}
