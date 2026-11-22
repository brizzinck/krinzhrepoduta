package ua.duikt.lavrishko.shakereminder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class ShakeSensorManager implements SensorEventListener {

    private static final String TAG = "ShakeReminder";

    public interface Listener {
        void onGForce(float gForce);
        void onShake();
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final ShakeDetector shakeDetector;
    private Listener listener;
    private boolean isListening = false;

    public ShakeSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            accelerometer = null;
        }
        shakeDetector = new ShakeDetector();
    }

    public boolean isAvailable() {
        return accelerometer != null;
    }

    public boolean start() {
        if (!isAvailable()) {
            return false;
        }
        if (!isListening) {
            isListening = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            if (isListening) {
                Log.d(TAG, "Listener registered");
            }
        }
        return isListening;
    }

    public void stop() {
        if (isListening && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isListening = false;
            Log.d(TAG, "Listener unregistered");
        }
    }

    public boolean isListening() {
        return isListening;
    }

    public void setSensitivity(float thresholdG) {
        shakeDetector.setThresholdG(thresholdG);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestampMs = SystemClock.elapsedRealtime();

        float gForce = ShakeDetector.toG(x, y, z);

        if (listener != null) {
            listener.onGForce(gForce);
        }

        if (shakeDetector.onSample(x, y, z, timestampMs)) {
            Log.d(TAG, "Shake detected");
            if (listener != null) {
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Не використовується
    }
}
