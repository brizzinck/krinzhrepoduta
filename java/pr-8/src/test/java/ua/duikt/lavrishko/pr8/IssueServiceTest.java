package ua.duikt.lavrishko.pr8;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {
    @Mock
    private TeamDirectory directory;   // стаб: лише повертає дані
    @Mock
    private ChatNotifier notifier;     // мок: перевіряємо виклик

    private IssueService service;

    @BeforeEach
    void setUp() {
        service = new IssueService(directory, notifier);
    }

    @Test
    void notifiesWhenTaskIsAdded() {
        when(directory.currentPoints("olena")).thenReturn(30);

        boolean added = service.plan("olena", 5);

        assertTrue(added);
        verify(notifier).send("olena", "Задачу на 5 SP додано в спринт");
    }

    @Test
    void notifiesWhenSprintIsFull() {
        when(directory.currentPoints("olena")).thenReturn(38);

        boolean added = service.plan("olena", 5);

        assertFalse(added);
        verify(notifier).send("olena", "Задача на 5 SP не вміщується в спринт (зараз 38 SP)");
    }
}
