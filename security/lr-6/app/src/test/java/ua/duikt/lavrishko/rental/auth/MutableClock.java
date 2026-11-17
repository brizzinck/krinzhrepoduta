package ua.duikt.lavrishko.rental.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/** Годинник для тестів: час можна «перемотати», тому тести не чекають по-справжньому. */
public class MutableClock extends Clock {
    private final Instant start;
    private final AtomicReference<Instant> now;

    public MutableClock(Instant start) {
        this.start = start;
        this.now = new AtomicReference<>(start);
    }

    /** Повертає годинник на початковий момент (контекст Spring спільний для всіх тестів). */
    public void reset() {
        now.set(start);
    }

    public void advance(Duration d) {
        now.updateAndGet(i -> i.plus(d));
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
