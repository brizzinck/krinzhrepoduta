package ua.duikt.lavrishko.shakereminder;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Шаблонний інструментальний тест, виконується на пристрої Android.
 *
 * @see <a href="http://d.android.com/tools/testing">Документація з тестування</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Контекст застосунку, що тестується.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("ua.duikt.lavrishko.shakereminder", appContext.getPackageName());
    }
}