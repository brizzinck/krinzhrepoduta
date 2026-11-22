package ua.duikt.lavrishko.shakereminder;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class ReminderTimer {

    private static final String TAG = "ShakeReminder";
    private static final long TICK_INTERVAL_MS = 100L;

    public interface OnTimerListener {
        void onTick(long remainingMs);
        void onFinish();
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private OnTimerListener listener;

    private boolean isRunning = false;
    private long endTimeMs = 0L;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) {
                return;
            }
            long remaining = getRemainingMs();
            if (remaining <= 0) {
                isRunning = false;
                Log.d(TAG, "Timer finished");
                if (listener != null) {
                    listener.onFinish();
                }
            } else {
                if (listener != null) {
                    listener.onTick(remaining);
                }
                handler.postDelayed(this, TICK_INTERVAL_MS);
            }
        }
    };

    public void setListener(OnTimerListener listener) {
        this.listener = listener;
    }

    public void start(long durationMs) {
        cancel();
        isRunning = true;
        endTimeMs = SystemClock.elapsedRealtime() + durationMs;
        Log.d(TAG, "Timer started");
        handler.post(tickRunnable);
    }

    public void cancel() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(tickRunnable);
            Log.d(TAG, "Timer cancelled");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long getRemainingMs() {
        if (!isRunning) {
            return 0L;
        }
        return Math.max(0L, endTimeMs - SystemClock.elapsedRealtime());
    }
}
