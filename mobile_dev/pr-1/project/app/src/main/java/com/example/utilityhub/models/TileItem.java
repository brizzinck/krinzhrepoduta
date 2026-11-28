package com.example.utilityhub.models;

// Одна плитка сітки: назва, іконка й Activity, яку вона відкриває (null для заглушки)
public class TileItem {
    private String title;
    private int iconResId;
    private Class<?> targetActivity;

    public TileItem(String title, int iconResId, Class<?> targetActivity) {
        this.title = title;
        this.iconResId = iconResId;
        this.targetActivity = targetActivity;
    }

    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public Class<?> getTargetActivity() { return targetActivity; }
}
