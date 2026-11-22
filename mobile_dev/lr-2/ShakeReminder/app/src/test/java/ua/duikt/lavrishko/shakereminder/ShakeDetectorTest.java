package ua.duikt.lavrishko.shakereminder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShakeDetectorTest {

    private static final float GRAVITY_EARTH = 9.81f;
    private ShakeDetector detector;

    @Before
    public void setUp() {
        detector = new ShakeDetector(); // поріг за замовчуванням 2,2 g, як у застосунку
    }

    private boolean simulateJolt(ShakeDetector detector, long timestampMs, float peakG) {
        float peakValue = peakG * GRAVITY_EARTH;
        detector.onSample(0, GRAVITY_EARTH, 0, timestampMs);
        return detector.onSample(0, peakValue, 0, timestampMs + 10);
    }

    @Test
    public void testSingleJoltDoesNotTrigger() {
        boolean result = simulateJolt(detector, 1000, 3.5f);
        assertFalse("Одиночний поштовх не повинен давати струшування", result);
    }

    @Test
    public void testThreeJoltsWithinOneSecondTriggers() {
        simulateJolt(detector, 1000, 3.5f);
        simulateJolt(detector, 1300, 3.5f);
        boolean result = simulateJolt(detector, 1600, 3.5f);
        assertTrue("Три поштовхи за 1 секунду повинні давати струшування", result);
    }

    @Test
    public void testThreeJoltsSpreadOverTwoSecondsDoesNotTrigger() {
        simulateJolt(detector, 1000, 3.5f);
        simulateJolt(detector, 2100, 3.5f);
        boolean result = simulateJolt(detector, 3200, 3.5f);
        assertFalse("Три поштовхи, розтягнуті на 2 секунди, не повинні давати струшування", result);
    }

    @Test
    public void testRepeatedTriggerBlockedDuringCooldown() {
        simulateJolt(detector, 1000, 3.5f);
        simulateJolt(detector, 1200, 3.5f);
        boolean initialShake = simulateJolt(detector, 1400, 3.5f);
        assertTrue("Перше струшування повинно спрацювати", initialShake);

        simulateJolt(detector, 1600, 3.5f);
        simulateJolt(detector, 1800, 3.5f);
        boolean cooldownShake = simulateJolt(detector, 2000, 3.5f);
        assertFalse("Струшування під час паузи повинно ігноруватися", cooldownShake);

        simulateJolt(detector, 3000, 3.5f);
        simulateJolt(detector, 3200, 3.5f);
        boolean afterCooldownShake = simulateJolt(detector, 3400, 3.5f);
        assertTrue("Струшування після паузи повинно спрацювати", afterCooldownShake);
    }

    @Test
    public void testNewThresholdChangesResult() {
        detector.setThresholdG(3.5f);

        simulateJolt(detector, 1000, 3.0f);
        simulateJolt(detector, 1200, 3.0f);
        boolean resultHighThreshold = simulateJolt(detector, 1400, 3.0f);
        assertFalse("Поштовхи по 3,0 g не повинні спрацьовувати на порозі 3,5 g", resultHighThreshold);

        detector.setThresholdG(2.5f);

        simulateJolt(detector, 3000, 3.0f);
        simulateJolt(detector, 3200, 3.0f);
        boolean resultLowThreshold = simulateJolt(detector, 3400, 3.0f);
        assertTrue("Поштовхи по 3,0 g повинні спрацьовувати на порозі 2,5 g", resultLowThreshold);
    }

    // Один удар може дати кілька відліків підряд вище порога: це все одно один поштовх.
    private boolean simulatePlateau(ShakeDetector detector, long timestampMs, int samples, float peakG) {
        float peakValue = peakG * GRAVITY_EARTH;
        boolean result = detector.onSample(0, GRAVITY_EARTH, 0, timestampMs);
        for (int i = 1; i <= samples; i++) {
            result |= detector.onSample(0, peakValue, 0, timestampMs + i * 10L);
        }
        return result;
    }

    @Test
    public void testLongPlateauCountsAsOneJolt() {
        boolean result = simulatePlateau(detector, 1000, 6, 3.5f);
        assertFalse("Шість відліків вище порога після одного удару є одним поштовхом", result);
        result = simulatePlateau(detector, 1300, 6, 3.5f);
        assertFalse("Два поштовхи ще не є струшуванням", result);
    }

    @Test
    public void testThreePlateausTrigger() {
        simulatePlateau(detector, 1000, 6, 3.5f);
        simulatePlateau(detector, 1300, 6, 3.5f);
        boolean result = simulatePlateau(detector, 1600, 6, 3.5f);
        assertTrue("Три поштовхи з довгими піками повинні давати струшування", result);
    }

    @Test
    public void testJitterAroundThresholdIsOneJolt() {
        // Значення коливаються навколо порога 2,2 g, але не спадають нижче 1,9 g: повторного поштовху немає.
        float[] gs = {2.3f, 2.1f, 2.3f, 2.1f, 2.3f, 2.1f, 2.3f};
        boolean result = false;
        for (int i = 0; i < gs.length; i++) {
            result |= detector.onSample(0, gs[i] * GRAVITY_EARTH, 0, 1000 + i * 20L);
        }
        assertFalse("Дрижання біля порога не повинно давати три поштовхи", result);
    }
}
