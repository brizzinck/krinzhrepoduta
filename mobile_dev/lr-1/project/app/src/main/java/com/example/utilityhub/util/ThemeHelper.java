package com.example.utilityhub.util;

import android.app.Activity;
import android.content.Context;
import com.example.utilityhub.R;
import com.google.android.material.color.MaterialColors;

// Спільна логіка вибору теми: усі Activity застосунку беруть тему з однакового місця.
public final class ThemeHelper {
    public static final String PREFS = "app_prefs";
    public static final String KEY_CUSTOM_THEME = "is_custom_theme";
    public static final String KEY_BG_URI = "bg_uri";

    private ThemeHelper() {}

    public static boolean isCustomTheme(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CUSTOM_THEME, false);
    }

    // Викликати в onCreate ДО super.onCreate()
    public static void applyTheme(Activity activity) {
        if (isCustomTheme(activity)) {
            activity.setTheme(R.style.Theme_UtilityHub_Custom);
        } else {
            activity.setTheme(R.style.Theme_UtilityHub);
        }
        // Тема накладається на вже створене вікно, тому колір рядка стану
        // береться зі старої теми. Задаємо його явно з colorPrimaryVariant поточної теми.
        int statusColor = MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorPrimaryVariant, 0xFF000000);
        activity.getWindow().setStatusBarColor(statusColor);
    }
}
