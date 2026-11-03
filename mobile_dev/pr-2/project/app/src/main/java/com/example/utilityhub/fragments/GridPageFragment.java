package com.example.utilityhub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.utilityhub.R;
import com.example.utilityhub.activities.CurrencyActivity;
import com.example.utilityhub.activities.SettingsActivity;
import com.example.utilityhub.activities.UnlockActivity;
import com.example.utilityhub.adapters.TileAdapter;
import com.example.utilityhub.databinding.FragmentGridPageBinding;
import com.example.utilityhub.models.TileItem;
import java.util.ArrayList;
import java.util.List;

public class GridPageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";
    private FragmentGridPageBinding binding;

    public static GridPageFragment newInstance(int pageIndex) {
        GridPageFragment fragment = new GridPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGridPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int pageIndex = getArguments() != null ? getArguments().getInt(ARG_PAGE_INDEX) : 0;
        List<TileItem> items = generateTilesForPage(pageIndex);

        binding.recyclerViewGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.recyclerViewGrid.setAdapter(new TileAdapter(getContext(), items));
    }

    private List<TileItem> generateTilesForPage(int pageIndex) {
        List<TileItem> list = new ArrayList<>();
        int startNumber = pageIndex * 9 + 1;
        for (int i = 0; i < 9; i++) {
            int tileNum = startNumber + i;
            if (tileNum == 1) {
                list.add(new TileItem("Конвертер валют", android.R.drawable.ic_menu_compass, CurrencyActivity.class));
            } else if (tileNum == 2) {
                list.add(new TileItem("Паролі", R.drawable.ic_password, UnlockActivity.class));
            } else if (tileNum == 3) {
                // Вхід в екран налаштувань (доповнення до ЛР 1: у методичці не передбачено)
                list.add(new TileItem("Налаштування", android.R.drawable.ic_menu_preferences, SettingsActivity.class));
            } else {
                list.add(new TileItem("Модуль #" + tileNum, android.R.drawable.ic_menu_help, null));
            }
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
