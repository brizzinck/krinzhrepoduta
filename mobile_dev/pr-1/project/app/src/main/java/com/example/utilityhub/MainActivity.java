package com.example.utilityhub;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.utilityhub.databinding.ActivityMainBinding;
import com.example.utilityhub.fragments.GridPageFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
}
