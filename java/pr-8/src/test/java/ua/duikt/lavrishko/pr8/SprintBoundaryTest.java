package ua.duikt.lavrishko.pr8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SprintBoundaryTest {
    private final Sprint sprint = new Sprint();

    // Межі оцінки (1, 13) і межі суми (разом 40 і 41): оцінка, поточна сума, очікуваний результат
    @ParameterizedTest
    @CsvSource({
            "1,  0,  true",    // найменша допустима оцінка
            "13, 0,  true",    // найбільша допустима оцінка
            "13, 27, true",    // разом рівно 40
            "13, 28, false"    // разом 41
    })
    void boundariesInsideRange(int points, int current, boolean expected) {
        assertEquals(expected, sprint.canAdd(points, current));
    }

    // Оцінки одразу поза діапазоном
    @ParameterizedTest
    @ValueSource(ints = {0, 14})
    void pointsOutsideRangeThrow(int points) {
        assertThrows(IllegalArgumentException.class, () -> sprint.canAdd(points, 0));
    }

    @Test
    void negativeCurrentThrows() {
        assertThrows(IllegalArgumentException.class, () -> sprint.canAdd(5, -1));
    }
}
