package ua.duikt.lavrishko.lr8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.vendor.issues.IssueStatus;
import ua.vendor.issues.OverduePolicy;
import ua.vendor.issues.Vendor;

/**
 * Приймальні тести OverduePolicy.check(deadline, done), варіант 2.
 * Написані за специфікацією, без перегляду коду збірок. Номер збірки: -Dbuild=N (за замовчуванням 1).
 * Годинник завжди фіксований, тому результат не залежить ні від поточної дати, ні від часового поясу комп'ютера.
 */
class OverduePolicyTest {
    private static final ZoneId KYIV = ZoneId.of("Europe/Kyiv");

    private static OverduePolicy policy(Clock clock) {
        return Vendor.build(Integer.getInteger("build", 1), clock);
    }

    /** Київський полудень заданої дати: далеко від півночі, тож дата однозначна в будь-якому поясі. */
    private static OverduePolicy policyOn(String today) {
        Instant noon = LocalDate.parse(today).atTime(12, 0).atZone(KYIV).toInstant();
        return policy(Clock.fixed(noon, KYIV));
    }

    /** Годинник, що показує заданий київський місцевий час. */
    private static Clock kyivClockAt(LocalDateTime kyivLocal) {
        return Clock.fixed(kyivLocal.atZone(KYIV).toInstant(), KYIV);
    }

    // Перевірка припущень тестів про календар; не залежить від збірки
    @Test
    void calendarAssumptions() {
        System.out.println("Часовий пояс комп'ютера: " + ZoneId.systemDefault());
        assertEquals(DayOfWeek.MONDAY, LocalDate.parse("2026-06-01").getDayOfWeek());
        assertEquals(DayOfWeek.FRIDAY, LocalDate.parse("2026-06-05").getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, LocalDate.parse("2026-10-25").getDayOfWeek());
        assertEquals(ZoneOffset.ofHours(3), KYIV.getRules().getOffset(Instant.parse("2026-10-24T21:30:00Z")));
        assertEquals(ZoneOffset.ofHours(2), KYIV.getRules().getOffset(Instant.parse("2026-10-25T21:30:00Z")));
    }

    // Виконана задача завжди ON_TRACK, навіть за давнього дедлайну
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({
            "дедлайн вчора, 2026-06-02, 2026-06-03",
            "дедлайн пʼятниця минулого тижня, 2026-05-29, 2026-06-03",
            "дедлайн кілька місяців тому, 2026-01-15, 2026-06-03"
    })
    void doneIssueIsOnTrack(String label, LocalDate deadline, LocalDate today) {
        assertEquals(IssueStatus.ON_TRACK, policyOn(today.toString()).check(deadline, true));
    }

    // Дедлайн сьогодні або пізніше: ON_TRACK
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({
            "дедлайн сьогодні (середа), 2026-06-03, 2026-06-03",
            "дедлайн сьогодні (субота), 2026-06-06, 2026-06-06",
            "дедлайн завтра, 2026-06-04, 2026-06-03",
            "дедлайн у понеділок - сьогодні неділя, 2026-06-08, 2026-06-07",
            "дедлайн через місяць, 2026-07-03, 2026-06-03"
    })
    void deadlineTodayOrLaterIsOnTrack(String label, LocalDate deadline, LocalDate today) {
        assertEquals(IssueStatus.ON_TRACK, policyOn(today.toString()).check(deadline, false));
    }

    // Невиконана прострочена задача: менше 2 робочих днів після дедлайну OVERDUE, 2 і більше ESCALATE.
    // Робочі дні: від дня після дедлайну до сьогодні включно, лише понеділок-п'ятниця.
    // Мітка рядка: день дедлайну - день сьогодні.
    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({
            "середа - четвер: 1 робочий день, 2026-06-03, 2026-06-04, OVERDUE",
            "понеділок - середа: 2 робочі дні, 2026-06-01, 2026-06-03, ESCALATE",
            "пʼятниця - понеділок: 1 робочий день, 2026-06-05, 2026-06-08, OVERDUE",
            "пʼятниця - вівторок: 2 робочі дні, 2026-06-05, 2026-06-09, ESCALATE",
            "четвер - субота: 1 (лише пʼятниця), 2026-06-04, 2026-06-06, OVERDUE",
            "четвер - неділя: 1 (лише пʼятниця), 2026-06-04, 2026-06-07, OVERDUE",
            "четвер - понеділок: 2 (пʼятниця й понеділок), 2026-06-04, 2026-06-08, ESCALATE",
            "пʼятниця - субота: 0 робочих днів, 2026-06-05, 2026-06-06, OVERDUE",
            "пʼятниця - неділя: 0 робочих днів, 2026-06-05, 2026-06-07, OVERDUE",
            "субота - понеділок: 1 робочий день, 2026-06-06, 2026-06-08, OVERDUE",
            "субота - вівторок: 2 робочі дні, 2026-06-06, 2026-06-09, ESCALATE",
            "неділя - понеділок: 1 робочий день, 2026-06-07, 2026-06-08, OVERDUE",
            "неділя - вівторок: 2 робочі дні, 2026-06-07, 2026-06-09, ESCALATE",
            "перехід через рік - 1 робочий день, 2025-12-31, 2026-01-01, OVERDUE",
            "перехід через рік - 2 робочі дні, 2025-12-31, 2026-01-02, ESCALATE",
            "перехід через місяць - 1 робочий день, 2026-07-31, 2026-08-03, OVERDUE",
            "перехід через місяць - 2 робочі дні, 2026-07-31, 2026-08-04, ESCALATE",
            "прострочено кілька тижнів, 2026-05-04, 2026-06-03, ESCALATE",
            "прострочено кілька місяців, 2026-01-15, 2026-06-03, ESCALATE"
    })
    void overdueByWorkingDays(String label, LocalDate deadline, LocalDate today, IssueStatus expected) {
        assertEquals(expected, policyOn(today.toString()).check(deadline, false));
    }

    // Дедлайн відсутній: IllegalArgumentException
    @Test
    void missingDeadlineThrowsForOpenIssue() {
        assertThrows(IllegalArgumentException.class, () -> policyOn("2026-06-03").check(null, false));
    }

    // Неоднозначність специфікації: для виконаної задачі правило «виконана: ON_TRACK» суперечить
    // правилу «дедлайн відсутній: некоректно». Читаю буквально: відсутній дедлайн некоректний завжди.
    @Test
    @Tag("interpretation")
    void missingDeadlineThrowsEvenForDoneIssue() {
        assertThrows(IllegalArgumentException.class, () -> policyOn("2026-06-03").check(null, true));
    }

    // Київська північ, літо (UTC+3): 00:30 середи 10.06 за Києвом, у UTC ще вівторок 09.06.
    // Дедлайн вівторок 09.06: у Києві минув 1 робочий день (середа), тож OVERDUE.
    // Ловить збірки, що беруть дату в UTC або зсув +2 без літнього часу (там «сьогодні» вівторок, ON_TRACK).
    @Test
    void kyivMidnightSummer() {
        OverduePolicy policy = policy(kyivClockAt(LocalDateTime.parse("2026-06-10T00:30:00")));
        assertEquals(IssueStatus.OVERDUE, policy.check(LocalDate.parse("2026-06-09"), false));
    }

    // Київ 23:30 взимку (UTC+2): ще середа 09.12. Дедлайн той самий день: ON_TRACK.
    // Ловить збірки із фіксованим зсувом +3 (там уже четвер 10.12, дедлайн минув).
    @Test
    void kyivLateEveningWinter() {
        OverduePolicy policy = policy(kyivClockAt(LocalDateTime.parse("2026-12-09T23:30:00")));
        assertEquals(IssueStatus.ON_TRACK, policy.check(LocalDate.parse("2026-12-09"), false));
    }

    // День переходу на зимовий час, 25.10.2026. Після 00:30 (ще UTC+3, у UTC 24.10) дата вже 25.10:
    // дедлайн субота 24.10 минув, 0 робочих днів, OVERDUE.
    @Test
    void dstDayAfterMidnight() {
        OverduePolicy policy = policy(kyivClockAt(LocalDateTime.parse("2026-10-25T00:30:00")));
        assertEquals(IssueStatus.OVERDUE, policy.check(LocalDate.parse("2026-10-24"), false));
    }

    // Той самий день о 23:30 (вже UTC+2): дата ще 25.10, дедлайн сьогодні: ON_TRACK.
    @Test
    void dstDayLateEvening() {
        OverduePolicy policy = policy(kyivClockAt(LocalDateTime.parse("2026-10-25T23:30:00")));
        assertEquals(IssueStatus.ON_TRACK, policy.check(LocalDate.parse("2026-10-25"), false));
    }

    // Тлумачення: та сама мить, що в kyivMidnightSummer, але годинник у зоні UTC. Специфікація каже, що
    // «сьогодні» визначається за київським часом, тож очікую київську дату (10.06) і OVERDUE.
    // Збірка, що бере дату з зони годинника, дасть 09.06 і ON_TRACK.
    @Test
    @Tag("interpretation")
    void kyivDateEvenWhenClockIsInUtc() {
        Instant instant = LocalDateTime.parse("2026-06-10T00:30:00").atZone(KYIV).toInstant();
        OverduePolicy policy = policy(Clock.fixed(instant, ZoneOffset.UTC));
        assertEquals(IssueStatus.OVERDUE, policy.check(LocalDate.parse("2026-06-09"), false));
    }
}
