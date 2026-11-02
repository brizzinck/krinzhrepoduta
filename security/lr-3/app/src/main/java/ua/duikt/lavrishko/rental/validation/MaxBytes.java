package ua.duikt.lavrishko.rental.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Довжина рядка в байтах UTF-8 не більша за задану (BCrypt обробляє лише перші 72 байти пароля). */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {
    int value();

    String message() default "Значення задовге у байтах";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
