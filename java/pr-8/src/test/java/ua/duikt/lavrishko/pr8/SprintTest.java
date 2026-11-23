package ua.duikt.lavrishko.pr8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SprintTest {
    private final Sprint sprint = new Sprint();

    @Test
    void addsTaskWhenItFits() {
        // Given: у спринті 30 SP, задача на 5 SP (разом 35 з 40)
        int points = 5;
        int current = 30;
        // When
        boolean result = sprint.canAdd(points, current);
        // Then
        assertEquals(true, result);
    }

    @Test
    void rejectsZeroPoints() {
        // Given: оцінка 0 поза діапазоном 1..13
        int points = 0;
        // When + Then
        assertThrows(IllegalArgumentException.class, () -> sprint.canAdd(points, 10));
    }

    @Test
    void rejectsTaskWhenSprintIsFull() {
        // Given: у спринті 38 SP, задача на 5 SP (разом 43 з 40)
        int points = 5;
        int current = 38;
        // When
        boolean result = sprint.canAdd(points, current);
        // Then
        assertEquals(false, result);
    }
}
