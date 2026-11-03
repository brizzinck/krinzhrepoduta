package com.example.utilityhub.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.utilityhub.databinding.ActivityPasswordEditBinding;
import com.example.utilityhub.models.PasswordItem;
import com.example.utilityhub.security.CryptoManager;
import com.example.utilityhub.security.VaultSession;
import com.example.utilityhub.storage.PasswordStorage;
import com.example.utilityhub.util.ThemeHelper;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PasswordEditActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "item_id";
    private ActivityPasswordEditBinding binding;
    private PasswordStorage storage;

    private final CryptoManager cryptoManager = new CryptoManager();

    private final SecureRandom secureRandom = new SecureRandom();
    private String editingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        if (!VaultSession.isUnlocked()) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
            return;
        }
        binding = ActivityPasswordEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = new PasswordStorage(this);
        configurePasswordVisibility();
        configureGenerator();
        editingId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (editingId != null) {
            binding.textEditTitle.setText("Редагування запису");
            loadItemForEditing(editingId);
        }
        binding.btnCopyPassword.setOnClickListener(v -> copyCurrentPassword());
        binding.btnSavePassword.setOnClickListener(v -> saveItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        VaultSession.lockIfExpired();
        // PasswordVaultActivity під цим екраном сама відкриє UnlockActivity.
        if (!VaultSession.isUnlocked()) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VaultSession.markPaused();
    }

    private void configurePasswordVisibility() {
        binding.checkShowPassword.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                binding.editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.editPassword.setSelection(
                    binding.editPassword.getText().length());
        });
    }

    private void configureGenerator() {
        updateLengthLabel();
        binding.seekPasswordLength.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int progress, boolean fromUser) {
                        updateLengthLabel();
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });
        binding.btnGeneratePassword.setOnClickListener(v -> {
            int length = 8 + binding.seekPasswordLength.getProgress();
            String generated = generatePassword(
                    length,
                    binding.checkLower.isChecked(),
                    binding.checkUpper.isChecked(),
                    binding.checkDigits.isChecked(),
                    binding.checkSymbols.isChecked());
            if (generated == null) {
                Toast.makeText(this,
                        "Оберіть хоча б одну групу символів",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            binding.editPassword.setText(generated);
            binding.editPassword.setSelection(generated.length());
        });
    }

    private void updateLengthLabel() {
        int length = 8 + binding.seekPasswordLength.getProgress();
        binding.textPasswordLength.setText("Довжина: " + length);
    }

    private String generatePassword(int length,
                                    boolean lower,
                                    boolean upper,
                                    boolean digits,
                                    boolean symbols) {
        List<String> groups = new ArrayList<>();
        if (lower) groups.add("abcdefghijklmnopqrstuvwxyz");
        if (upper) groups.add("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (digits) groups.add("0123456789");
        if (symbols) groups.add("!@#$%^&*()-_=+?");
        if (groups.isEmpty()) {
            return null;
        }
        StringBuilder pool = new StringBuilder();
        for (String group : groups) {
            pool.append(group);
        }
        List<Character> chars = new ArrayList<>();
        // Гарантуємо хоча б один символ з кожної обраної групи.
        for (String group : groups) {
            chars.add(group.charAt(
                    secureRandom.nextInt(group.length())));
        }
        while (chars.size() < length) {
            chars.add(pool.charAt(
                    secureRandom.nextInt(pool.length())));
        }
        // Перемішування Fisher-Yates із SecureRandom.
        for (int i = chars.size() - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            Character temp = chars.get(i);
            chars.set(i, chars.get(j));
            chars.set(j, temp);
        }
        StringBuilder result = new StringBuilder();
        for (Character c : chars) {
            result.append(c);
        }
        return result.toString();
    }

    private void loadItemForEditing(String id) {
        for (PasswordItem item : storage.load()) {
            if (item.getId().equals(id)) {
                binding.editService.setText(item.getService());
                binding.editLogin.setText(item.getLogin());
                binding.editNote.setText(item.getNote());
                try {
                    binding.editPassword.setText(
                            cryptoManager.decrypt(item.getEncryptedPassword()));
                } catch (Exception e) {
                    Toast.makeText(this,
                            "Не вдалося розшифрувати запис",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
        Toast.makeText(this,
                "Запис не знайдено",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void copyCurrentPassword() {
        String password = binding.editPassword.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(this,
                    "Пароль порожній",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText("password", password));
        Toast.makeText(this,
                "Пароль скопійовано",
                Toast.LENGTH_SHORT).show();
    }

    private void saveItem() {
        String service = binding.editService.getText().toString().trim();
        String login = binding.editLogin.getText().toString().trim();
        String password = binding.editPassword.getText().toString();
        String note = binding.editNote.getText().toString().trim();
        if (service.isEmpty()) {
            binding.editService.setError("Вкажіть назву сервісу");
            return;
        }
        if (password.isEmpty()) {
            binding.editPassword.setError("Введіть або згенеруйте пароль");
            return;
        }
        try {
            String encrypted = cryptoManager.encrypt(password);
            List<PasswordItem> items = storage.load();
            if (editingId == null) {
                items.add(new PasswordItem(
                        UUID.randomUUID().toString(),
                        service,
                        login,
                        encrypted,
                        note));
            } else {
                boolean updated = false;
                for (PasswordItem item : items) {
                    if (item.getId().equals(editingId)) {
                        item.setService(service);
                        item.setLogin(login);
                        item.setEncryptedPassword(encrypted);
                        item.setNote(note);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Toast.makeText(this,
                            "Запис для редагування не знайдено",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            storage.save(items);
            binding.editPassword.setText("");
            finish();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Не вдалося зашифрувати та зберегти пароль",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
