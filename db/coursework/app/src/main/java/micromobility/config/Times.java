package micromobility.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** The database holds UTC; people see and type Europe/Kyiv (D11). */
public final class Times {

    public static final ZoneId KYIV = ZoneId.of("Europe/Kyiv");
    private static final DateTimeFormatter VIEW = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Times() {
    }

    /** Start of the given Kyiv day as a UTC time; null stays null (no filter). */
    public static LocalDateTime startUtc(LocalDate kyivDay) {
        return kyivDay == null ? null : kyivDay.atStartOfDay(KYIV).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /** The end of the filter period: the start of the day after the given Kyiv day (the "to" date is inclusive). */
    public static LocalDateTime endUtc(LocalDate kyivDay) {
        return kyivDay == null ? null : startUtc(kyivDay.plusDays(1));
    }

    /** A stored UTC time shown in Kyiv time. */
    public static String view(LocalDateTime utc) {
        return utc == null ? "" : utc.atZone(ZoneOffset.UTC).withZoneSameInstant(KYIV).format(VIEW);
    }
}
