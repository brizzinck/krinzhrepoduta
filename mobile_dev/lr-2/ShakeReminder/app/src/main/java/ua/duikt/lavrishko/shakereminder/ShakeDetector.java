package ua.duikt.lavrishko.shakereminder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Чистий Java-клас без Android API: визначає жест струшування за відліками акселерометра.
 */
public class ShakeDetector {

    private static final float GRAVITY_EARTH = 9.81f;
    private static final long JOLT_TIME_WINDOW_MS = 1000L;
    private static final long COOLDOWN_TIME_MS = 1500L;
    private static final int MIN_JOLTS = 3;
    // Наступний поштовх рахується лише після спаду нижче порога на цю величину.
    private static final float HYSTERESIS_G = 0.3f;

    private float thresholdG = 2.2f;
    private boolean armed = true;
    private long lastShakeTimestampMs = 0L;
    private final List<Long> joltTimestamps = new ArrayList<>();

    public ShakeDetector() {
    }

    public ShakeDetector(float thresholdG) {
        this.thresholdG = thresholdG;
    }

    /** Перераховує прискорення (м/с^2) за трьома осями в одиниці g. */
    public static float toG(float x, float y, float z) {
        return (float) (Math.sqrt(x * x + y * y + z * z) / GRAVITY_EARTH);
    }

    /**
     * Обробляє один відлік акселерометра.
     *
     * @param x           прискорення по осі X (м/с^2)
     * @param y           прискорення по осі Y (м/с^2)
     * @param z           прискорення по осі Z (м/с^2)
     * @param timestampMs мітка часу відліку в мілісекундах
     * @return true, якщо виявлено струшування
     */
    public boolean onSample(float x, float y, float z, long timestampMs) {
        float gForce = toG(x, y, z);

        // Після спаду нижче порога детектор знову готовий рахувати поштовх.
        if (gForce < thresholdG - HYSTERESIS_G) {
            armed = true;
        }

        // Пауза 1500 мс після спрацювання.
        if (lastShakeTimestampMs > 0 && (timestampMs - lastShakeTimestampMs < COOLDOWN_TIME_MS)) {
            return false;
        }

        // Поштовх: перетин порога знизу вгору. Решта відліків вище порога того самого удару не рахується.
        if (armed && gForce >= thresholdG) {
            armed = false;
            joltTimestamps.add(timestampMs);
        }

        // Видаляємо поштовхи старші за 1000 мс.
        Iterator<Long> iterator = joltTimestamps.iterator();
        while (iterator.hasNext()) {
            if (timestampMs - iterator.next() > JOLT_TIME_WINDOW_MS) {
                iterator.remove();
            } else {
                break;
            }
        }

        // Струшування: щонайменше 3 поштовхи за 1000 мс.
        if (joltTimestamps.size() >= MIN_JOLTS) {
            lastShakeTimestampMs = timestampMs;
            joltTimestamps.clear();
            return true;
        }

        return false;
    }

    public void setThresholdG(float thresholdG) {
        this.thresholdG = thresholdG;
    }

    public float getThresholdG() {
        return thresholdG;
    }

    public void reset() {
        armed = true;
        lastShakeTimestampMs = 0L;
        joltTimestamps.clear();
    }
}
