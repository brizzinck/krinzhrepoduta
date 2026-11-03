package com.example.utilityhub.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.utilityhub.adapters.PasswordAdapter;
import com.example.utilityhub.databinding.ActivityPasswordVaultBinding;
import com.example.utilityhub.models.PasswordItem;
import com.example.utilityhub.security.VaultSession;
import com.example.utilityhub.storage.PasswordStorage;
import com.example.utilityhub.util.ThemeHelper;
import java.util.ArrayList;
import java.util.List;

public class PasswordVaultActivity extends AppCompatActivity {
    private ActivityPasswordVaultBinding binding;

    private final List<PasswordItem> items = new ArrayList<>();
    private PasswordAdapter adapter;
    private PasswordStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        if (!VaultSession.isUnlocked()) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
            return;
        }
        binding = ActivityPasswordVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = new PasswordStorage(this);
        adapter = new PasswordAdapter(
                this,
                items,
                new PasswordAdapter.OnPasswordActionListener() {
                    @Override
                    public void onEdit(PasswordItem item) {
                        Intent intent = new Intent(
                                PasswordVaultActivity.this,
                                PasswordEditActivity.class);
                        intent.putExtra(
                                PasswordEditActivity.EXTRA_ITEM_ID,
                                item.getId());
                        startActivity(intent);
                    }
                    @Override
                    public void onDelete(PasswordItem item) {
                        confirmDelete(item);
                    }
                });
        binding.recyclerPasswords.setLayoutManager(
                new LinearLayoutManager(this));
        binding.recyclerPasswords.setAdapter(adapter);
        binding.btnAddPassword.setOnClickListener(v ->
                startActivity(new Intent(
                        this, PasswordEditActivity.class)));
        binding.btnLockVault.setOnClickListener(v -> {
            VaultSession.lock();
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storage == null) {
            return;
        }
        VaultSession.lockIfExpired();
        if (!VaultSession.isUnlocked()) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
            return;
        }
        items.clear();
        items.addAll(storage.load());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VaultSession.markPaused();
    }

    private void confirmDelete(PasswordItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Видалити запис?")
                .setMessage(item.getService())
                .setPositiveButton("Видалити", (dialog, which) -> {
                    for (int i = items.size() - 1; i >= 0; i--) {
                        if (items.get(i).getId().equals(item.getId())) {
                            items.remove(i);
                            break;
                        }
                    }
                    storage.save(items);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }
}
