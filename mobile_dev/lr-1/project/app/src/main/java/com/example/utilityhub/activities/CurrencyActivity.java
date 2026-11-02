package com.example.utilityhub.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.utilityhub.databinding.ActivityCurrencyBinding;
import com.example.utilityhub.util.ThemeHelper;
import java.util.Locale;

public class CurrencyActivity extends AppCompatActivity {
    private ActivityCurrencyBinding binding;
    private final String[] currencies = {"USD в EUR (0.92)", "EUR в USD (1.09)", "USD в UAH (41.50)"};
    private final double[] rates = {0.92, 1.09, 41.50};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityCurrencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        binding.spinnerCurrency.setAdapter(adapter);

        binding.buttonConvert.setOnClickListener(v -> performConversion());
    }

    private void performConversion() {
        String inputStr = binding.editAmount.getText().toString().trim();
        if (inputStr.isEmpty()) {
            Toast.makeText(this, "Будь ласка, введіть суму!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(inputStr);
            int selectedIndex = binding.spinnerCurrency.getSelectedItemPosition();
            double result = amount * rates[selectedIndex];
            binding.textResult.setText(String.format(Locale.getDefault(), "Результат: %.2f", result));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некоректний формат числа", Toast.LENGTH_SHORT).show();
        }
    }
}
