package ua.duikt.lavrishko.shakereminder;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {

    private static final String PREF_NAME = "shake_reminder_prefs";
    private static final String KEY_DURATION = "key_duration_seconds";
    private static final String KEY_SENSITIVITY = "key_sensitivity_g";

    private static final int DEFAULT_DURATION = 10;
    private static final float DEFAULT_SENSITIVITY = 2.2f;

    private final SharedPreferences prefs;

    public SettingsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getDurationSeconds() {
        return prefs.getInt(KEY_DURATION, DEFAULT_DURATION);
    }

    public void setDurationSeconds(int seconds) {
        prefs.edit().putInt(KEY_DURATION, seconds).apply();
    }

    public float getSensitivityG() {
        return prefs.getFloat(KEY_SENSITIVITY, DEFAULT_SENSITIVITY);
    }

    public void setSensitivityG(float g) {
        prefs.edit().putFloat(KEY_SENSITIVITY, g).apply();
    }
}
