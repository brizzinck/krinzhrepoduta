package com.example.utilityhub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.utilityhub.databinding.ActivitySettingsBinding;
import com.example.utilityhub.util.ThemeHelper;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    // Лаунчер для вибору зображення з галереї/пам'яті
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Зберігаємо URI файлу та надаємо йому персистентні права доступу
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {}
                    prefs.edit().putString("bg_uri", uri.toString()).apply();
                    Toast.makeText(this, "Фон успішно оновлено!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        // Встановлюємо вибрану тему перед супер-викликом
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Перемикання теми
        binding.btnToggleTheme.setOnClickListener(v -> {
            boolean current = prefs.getBoolean("is_custom_theme", false);
            prefs.edit().putBoolean("is_custom_theme", !current).apply();
            recreate(); // Перезапуск Activity для застосування нової теми
        });

        // Вибір фонового зображення
        binding.btnSelectBackground.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        // Скидання фонового зображення
        binding.btnResetBackground.setOnClickListener(v -> {
            prefs.edit().remove("bg_uri").apply();
            Toast.makeText(this, "Фон скинуто", Toast.LENGTH_SHORT).show();
        });
    }
}
