package ua.duikt.lavrishko.rental.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Модульні тести власних анотацій. Час зафіксовано, щоб результат не залежав від дня запуску. */
class CustomValidatorsTest {
    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC);

    private static class Holder {
        @BirthDate(minYear = 1900, minAge = 18)
        LocalDate birth;
        @MaxBytes(72)
        String text;
    }

    private BirthDateValidator birth;
    private MaxBytesValidator bytes;
    private final ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

    @BeforeEach
    void setUp() throws Exception {
        birth = new BirthDateValidator(FIXED);
        birth.initialize(Holder.class.getDeclaredField("birth").getAnnotation(BirthDate.class));
        bytes = new MaxBytesValidator();
        bytes.initialize(Holder.class.getDeclaredField("text").getAnnotation(MaxBytes.class));
    }

    @Test
    void dateInFutureIsRejected() {
        assertFalse(birth.isValid(LocalDate.of(2026, 11, 3), ctx));
        assertFalse(birth.isValid(LocalDate.of(2099, 1, 1), ctx));
    }

    @Test
    void youngerThan18IsRejectedAndExactly18IsAccepted() {
        assertFalse(birth.isValid(LocalDate.of(2008, 11, 3), ctx));
        assertTrue(birth.isValid(LocalDate.of(2008, 11, 2), ctx));
    }

    @Test
    void tooOldDateIsRejected() {
        assertFalse(birth.isValid(LocalDate.of(1899, 12, 31), ctx));
        assertTrue(birth.isValid(LocalDate.of(1900, 1, 1), ctx));
    }

    @Test
    void maxBytesCountsUtf8BytesNotCharacters() {
        // 40 кириличних літер = 80 байтів у UTF-8, хоча лише 40 символів
        assertFalse(bytes.isValid("Ж".repeat(40), ctx));
        assertTrue(bytes.isValid("Z".repeat(72), ctx));
        assertFalse(bytes.isValid("Z".repeat(73), ctx));
    }
}
