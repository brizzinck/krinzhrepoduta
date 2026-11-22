package ua.duikt.lavrishko.shakereminder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import ua.duikt.lavrishko.shakereminder.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShakeReminder";

    private ActivityMainBinding binding;
    private ShakeViewModel viewModel;
    private ShakeSensorManager sensorManager;
    private SettingsStore settingsStore;
    private AlertPlayer alertPlayer;
    private ReminderNotifier reminderNotifier;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private boolean isAppResumed = false;
    private boolean hasPromptedPermission = false;
    private long lastGForceUpdateMs = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ShakeViewModel.class);
        settingsStore = new SettingsStore(this);
        sensorManager = new ShakeSensorManager(this);
        alertPlayer = new AlertPlayer(this);
        reminderNotifier = new ReminderNotifier(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        showHint(R.string.hint_timer_started);
                    } else {
                        showHint(R.string.hint_permission_denied);
                    }
                }
        );

        if (savedInstanceState == null) {
            viewModel.setDurationSeconds(settingsStore.getDurationSeconds());
            viewModel.setSensitivityG(settingsStore.getSensitivityG());
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.sliderSensitivity.setValue(viewModel.getSensitivityG());
        binding.tvSensitivityLabel.setText(getString(R.string.sensitivity_label, viewModel.getSensitivityG()));

        // Правка студента: до першого відліку датчика у полі показувався шаблон «%.2f g».
        showGForce(viewModel.getCurrentGForce());
        binding.etDuration.setText(String.valueOf(viewModel.getDurationSeconds()));
        binding.tvShakeCount.setText(getString(R.string.shake_count_format, viewModel.getShakeCount()));

        // Правка студента: після повороту відновлюються підказка і стан таймера.
        binding.tvHint.setText(viewModel.getHintResId());
        if (viewModel.getTimer().isRunning()) {
            updateTimerUI(viewModel.getTimer().getRemainingMs());
        } else if (viewModel.isTimerFinished()) {
            binding.tvTimer.setText(R.string.timer_finished);
        } else {
            binding.tvTimer.setText(R.string.timer_idle);
        }

        if (!sensorManager.isAvailable()) {
            binding.tvSensorStatus.setText(R.string.sensor_status_unavailable);
            binding.btnToggle.setEnabled(false);
            showHint(R.string.sensor_status_unavailable);
        } else {
            if (viewModel.isTracking()) {
                binding.btnToggle.setText(R.string.btn_stop_tracking);
                binding.tvSensorStatus.setText(R.string.sensor_status_tracking);
            } else {
                binding.btnToggle.setText(R.string.btn_start_tracking);
                binding.tvSensorStatus.setText(R.string.sensor_status_idle);
            }
        }
    }

    private void setupListeners() {
        binding.sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            viewModel.setSensitivityG(value);
            settingsStore.setSensitivityG(value);
            sensorManager.setSensitivity(value);
            binding.tvSensitivityLabel.setText(getString(R.string.sensitivity_label, value));
        });

        binding.etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDuration();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnToggle.setOnClickListener(v -> {
            if (!sensorManager.isAvailable()) {
                return;
            }
            boolean newTracking = !viewModel.isTracking();
            viewModel.setTracking(newTracking);
            if (newTracking) {
                startTracking();
            } else {
                stopTracking();
            }
        });

        viewModel.getTimer().setListener(new ReminderTimer.OnTimerListener() {
            @Override
            public void onTick(long remainingMs) {
                updateTimerUI(remainingMs);
            }

            @Override
            public void onFinish() {
                viewModel.setTimerFinished(true);
                binding.tvTimer.setText(R.string.timer_finished);
                showHint(R.string.hint_timer_finished);
                if (alertPlayer != null) {
                    alertPlayer.playAlert();
                }
                if (!isAppResumed && reminderNotifier != null) {
                    reminderNotifier.showNotification();
                }
            }
        });

        sensorManager.setListener(new ShakeSensorManager.Listener() {
            @Override
            public void onGForce(float gForce) {
                viewModel.setCurrentGForce(gForce);
                long currentTime = android.os.SystemClock.elapsedRealtime();
                if (currentTime - lastGForceUpdateMs >= 100L) {
                    lastGForceUpdateMs = currentTime;
                    showGForce(gForce);
                }
            }

            @Override
            public void onShake() {
                handleShakeEvent();
            }
        });
    }

    private void startTracking() {
        sensorManager.setSensitivity(viewModel.getSensitivityG());
        sensorManager.start();
        binding.btnToggle.setText(R.string.btn_stop_tracking);
        binding.tvSensorStatus.setText(R.string.sensor_status_tracking);
    }

    private void stopTracking() {
        sensorManager.stop();
        viewModel.getTimer().cancel();
        viewModel.setTimerFinished(false);
        viewModel.setCurrentGForce(Float.NaN);
        showGForce(Float.NaN);
        binding.btnToggle.setText(R.string.btn_start_tracking);
        binding.tvSensorStatus.setText(R.string.sensor_status_idle);
        binding.tvTimer.setText(R.string.timer_idle);
        showHint(R.string.hint_default);
    }

    private void handleShakeEvent() {
        viewModel.incrementShakeCount();
        binding.tvShakeCount.setText(getString(R.string.shake_count_format, viewModel.getShakeCount()));

        ReminderTimer timer = viewModel.getTimer();
        if (!timer.isRunning()) {
            Integer durationSec = validateDuration();
            if (durationSec != null) {
                checkAndRequestNotificationPermission();
                viewModel.setTimerFinished(false);
                timer.start(durationSec * 1000L);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    showHint(R.string.hint_timer_started);
                }
            } else {
                Log.d(TAG, "Timer start failed: invalid duration");
            }
        } else {
            timer.cancel();
            binding.tvTimer.setText(R.string.timer_idle);
            showHint(R.string.hint_timer_cancelled);
        }
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!hasPromptedPermission) {
                    hasPromptedPermission = true;
                    showHint(R.string.hint_permission_rationale);
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }

    private Integer validateDuration() {
        String text = binding.etDuration.getText() != null ? binding.etDuration.getText().toString().trim() : "";
        if (text.isEmpty()) {
            binding.tilDuration.setError(getString(R.string.duration_error));
            return null;
        }
        try {
            int val = Integer.parseInt(text);
            if (val < 5 || val > 600) {
                binding.tilDuration.setError(getString(R.string.duration_error));
                return null;
            }
            binding.tilDuration.setError(null);
            viewModel.setDurationSeconds(val);
            settingsStore.setDurationSeconds(val);
            return val;
        } catch (NumberFormatException e) {
            binding.tilDuration.setError(getString(R.string.duration_error));
            return null;
        }
    }

    // Показує підказку і запам'ятовує її у ViewModel, щоб поворот екрана її не стер.
    private void showHint(int resId) {
        viewModel.setHintResId(resId);
        binding.tvHint.setText(resId);
    }

    // Показує прискорення в g; до першого відліку датчика і після «Зупинити» значення невідоме.
    private void showGForce(float gForce) {
        if (Float.isNaN(gForce)) {
            binding.tvGForce.setText(R.string.g_force_unknown);
            binding.pbGForce.setProgress(0);
            return;
        }
        binding.tvGForce.setText(String.format(Locale.getDefault(), getString(R.string.g_force_format), gForce));
        binding.pbGForce.setProgress(Math.min(50, Math.max(0, (int) (gForce * 10))));
    }

    private void updateTimerUI(long remainingMs) {
        long seconds = (remainingMs + 999L) / 1000L;
        long mins = seconds / 60;
        long secs = seconds % 60;
        binding.tvTimer.setText(String.format(Locale.getDefault(), getString(R.string.timer_running_format), mins, secs));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppResumed = true;
        if (sensorManager.isAvailable() && viewModel.isTracking()) {
            sensorManager.setSensitivity(viewModel.getSensitivityG());
            sensorManager.start();
            binding.btnToggle.setText(R.string.btn_stop_tracking);
            binding.tvSensorStatus.setText(R.string.sensor_status_tracking);
        } else if (!sensorManager.isAvailable()) {
            binding.tvSensorStatus.setText(R.string.sensor_status_unavailable);
            binding.btnToggle.setEnabled(false);
        }

        binding.tvShakeCount.setText(getString(R.string.shake_count_format, viewModel.getShakeCount()));
        if (viewModel.getTimer().isRunning()) {
            updateTimerUI(viewModel.getTimer().getRemainingMs());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                showHint(R.string.hint_timer_started);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppResumed = false;
        if (sensorManager.isListening()) {
            sensorManager.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null && viewModel.getTimer() != null) {
            viewModel.getTimer().setListener(null);
        }
        if (sensorManager != null) {
            sensorManager.setListener(null);
        }
        if (alertPlayer != null) {
            alertPlayer.release();
            alertPlayer = null;
        }
    }
}
