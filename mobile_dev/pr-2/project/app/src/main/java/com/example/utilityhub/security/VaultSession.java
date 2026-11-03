package com.example.utilityhub.security;

import android.os.SystemClock;

public final class VaultSession {
    // Додаткове завдання: сесія закривається, якщо сховище довше 30 с не на екрані.
    private static final long TIMEOUT_MS = 30000;
    private static boolean unlocked = false;
    private static long pausedAt = 0;

    private VaultSession() { }

    public static void unlock() {
        unlocked = true;
        pausedAt = 0;
    }

    public static void lock() {
        unlocked = false;
    }

    public static boolean isUnlocked() {
        return unlocked;
    }

    // Викликати в onPause() екранів сховища.
    public static void markPaused() {
        pausedAt = SystemClock.elapsedRealtime();
    }

    // Викликати в onResume() екранів сховища.
    public static void lockIfExpired() {
        if (pausedAt != 0 && SystemClock.elapsedRealtime() - pausedAt > TIMEOUT_MS) {
            unlocked = false;
        }
        pausedAt = 0;
    }
}
