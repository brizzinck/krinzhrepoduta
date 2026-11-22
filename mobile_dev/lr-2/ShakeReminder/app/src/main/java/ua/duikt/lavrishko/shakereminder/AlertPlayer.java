package ua.duikt.lavrishko.shakereminder;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

public class AlertPlayer {

    private static final String TAG = "ShakeReminder";
    private final Context context;
    private ToneGenerator toneGenerator;

    public AlertPlayer(Context context) {
        this.context = context.getApplicationContext();
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e);
            toneGenerator = null;
        }
    }

    public void playAlert() {
        if (toneGenerator != null) {
            try {
                if (toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 300)) {
                    Log.d(TAG, "Alert tone started");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error playing tone", e);
            }
        }

        try {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
                Log.d(TAG, "Alert vibration started");
            } else {
                Log.d(TAG, "Alert vibration skipped: no vibrator");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error vibrating", e);
        }
    }

    public void release() {
        if (toneGenerator != null) {
            try {
                toneGenerator.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing ToneGenerator", e);
            }
            toneGenerator = null;
        }
    }
}
