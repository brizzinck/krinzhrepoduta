package com.example.utilityhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.utilityhub.databinding.ActivityMainBinding;
import com.example.utilityhub.fragments.GridPageFragment;
import com.example.utilityhub.util.ThemeHelper;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    // Яка тема застосована до цієї Activity (щоб помітити зміну після повернення з налаштувань)
    private boolean appliedCustomTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        appliedCustomTheme = ThemeHelper.isCustomTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Налаштування ViewPager2 на 2 сторінки
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return GridPageFragment.newInstance(position);
            }

            @Override
            public int getItemCount() {
                return 2; // Дві сторінки сітки (по 9 плиток)
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Тему змінено в налаштуваннях: перестворюємо головний екран з новою темою
        if (ThemeHelper.isCustomTheme(this) != appliedCustomTheme) {
            recreate();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(ThemeHelper.PREFS, Context.MODE_PRIVATE);
        String bgUriStr = prefs.getString(ThemeHelper.KEY_BG_URI, null);
        if (bgUriStr != null) {
            try {
                Uri bgUri = Uri.parse(bgUriStr);
                InputStream inputStream = getContentResolver().openInputStream(bgUri);
                Drawable drawable = Drawable.createFromStream(inputStream, bgUri.toString());
                binding.viewPager.setBackground(drawable);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                // Файл недоступний (видалено або втрачено право доступу): прибираємо фон
                binding.viewPager.setBackground(null);
            }
        } else {
            binding.viewPager.setBackground(null);
        }
    }
}
