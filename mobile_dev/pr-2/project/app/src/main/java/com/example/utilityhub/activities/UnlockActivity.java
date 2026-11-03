package com.example.utilityhub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.example.utilityhub.databinding.ActivityUnlockBinding;
import com.example.utilityhub.security.PinSecurityHelper;
import com.example.utilityhub.security.VaultSession;
import com.example.utilityhub.util.ThemeHelper;
import java.util.concurrent.Executor;

public class UnlockActivity extends AppCompatActivity {
    private static final String PREFS = "vault_prefs";
    private static final String KEY_METHOD = "security_method";
    private ActivityUnlockBinding binding;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityUnlockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences prefs = getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
        String method = prefs.getString(KEY_METHOD, null);
        if (method == null) {
            startActivity(new Intent(this, SecuritySetupActivity.class));
            finish();
            return;
        }
        if ("PIN".equals(method)) {
            showPinMode();
        } else if ("BIOMETRIC".equals(method)) {
            prepareBiometric();
            showBiometricMode();
        } else {
            prefs.edit().remove(KEY_METHOD).apply();
            startActivity(new Intent(this, SecuritySetupActivity.class));
            finish();
        }
    }

    private void showPinMode() {
        binding.pinUnlockContainer.setVisibility(View.VISIBLE);
        binding.btnRetryBiometric.setVisibility(View.GONE);
        binding.textUnlockStatus.setText("Введіть PIN, створений під час першого запуску.");
        binding.btnUnlockPin.setOnClickListener(v -> verifyPin());
    }

    private void verifyPin() {
        String pin = binding.editUnlockPin.getText().toString();
        if (pin.isEmpty()) {
            binding.editUnlockPin.setError("Введіть PIN");
            return;
        }
        try {
            if (PinSecurityHelper.verifyPin(this, pin)) {
                openVault();
            } else {
                binding.editUnlockPin.setError("Неправильний PIN");
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Помилка перевірки PIN",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        openVault();
                    }
                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        binding.textUnlockStatus.setText(
                                "Біометрію не розпізнано. Спробуйте ще раз.");
                    }
                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        binding.textUnlockStatus.setText(errString);
                    }
                });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Вхід до сховища паролів")
                .setSubtitle("Підтвердьте особу біометрією")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Скасувати")
                .build();
    }

    private void showBiometricMode() {
        binding.pinUnlockContainer.setVisibility(View.GONE);
        binding.btnRetryBiometric.setVisibility(View.VISIBLE);
        binding.textUnlockStatus.setText(
                "Підтвердьте особу системним біометричним діалогом.");
        binding.btnRetryBiometric.setOnClickListener(v -> authenticateBiometric());
        authenticateBiometric();
    }

    private void authenticateBiometric() {
        BiometricManager manager = BiometricManager.from(this);
        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            binding.textUnlockStatus.setText(
                    "Біометрія недоступна. Перевірте налаштування Android.");
        }
    }

    private void openVault() {
        VaultSession.unlock();
        startActivity(new Intent(this, PasswordVaultActivity.class));
        finish();
    }
}
