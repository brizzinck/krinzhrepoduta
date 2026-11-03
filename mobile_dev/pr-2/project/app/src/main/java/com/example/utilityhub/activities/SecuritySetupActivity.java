package com.example.utilityhub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import com.example.utilityhub.databinding.ActivitySecuritySetupBinding;
import com.example.utilityhub.security.PinSecurityHelper;
import com.example.utilityhub.util.ThemeHelper;

public class SecuritySetupActivity extends AppCompatActivity {
    private static final String PREFS = "vault_prefs";
    private static final String KEY_METHOD = "security_method";
    private ActivitySecuritySetupBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySecuritySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // Якщо спосіб уже налаштований, повторне первинне налаштування не потрібне.
        if (prefs.getString(KEY_METHOD, null) != null) {
            openUnlock();
            return;
        }
        binding.radioGroupSecurity.setOnCheckedChangeListener((group, checkedId) -> {
            boolean pinSelected = checkedId == binding.radioPin.getId();
            binding.pinFieldsContainer.setVisibility(
                    pinSelected ? View.VISIBLE : View.GONE);
        });
        binding.btnSaveSecurity.setOnClickListener(v -> saveSecurityMethod());
    }

    private void saveSecurityMethod() {
        int checkedId = binding.radioGroupSecurity.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this,
                    "Оберіть PIN або біометрію",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkedId == binding.radioPin.getId()) {
            savePinMethod();
        } else if (checkedId == binding.radioBiometric.getId()) {
            saveBiometricMethod();
        }
    }

    private void savePinMethod() {
        String pin = binding.editPinCreate.getText().toString();
        String confirm = binding.editPinConfirm.getText().toString();
        if (!pin.matches("\\d{4,8}")) {
            binding.editPinCreate.setError("Введіть від 4 до 8 цифр");
            // Текст помилки видно лише на полі у фокусі.
            binding.editPinCreate.requestFocus();
            return;
        }
        if (!pin.equals(confirm)) {
            binding.editPinConfirm.setError("PIN-коди не збігаються");
            return;
        }
        try {
            PinSecurityHelper.savePin(this, pin);
            prefs.edit().putString(KEY_METHOD, "PIN").apply();
            openUnlock();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Не вдалося зберегти PIN",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBiometricMethod() {
        BiometricManager manager = BiometricManager.from(this);
        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (result != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this,
                    // Android 12+ показує в Toast лише два рядки, тому текст короткий.
                    "Біометрія недоступна. Налаштуйте її в Android або оберіть PIN.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        prefs.edit().putString(KEY_METHOD, "BIOMETRIC").apply();
        openUnlock();
    }

    private void openUnlock() {
        startActivity(new Intent(this, UnlockActivity.class));
        finish();
    }
}
