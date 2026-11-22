package ua.duikt.lavrishko.shakereminder;

import androidx.lifecycle.ViewModel;

public class ShakeViewModel extends ViewModel {

    private final ReminderTimer timer = new ReminderTimer();
    private boolean isTracking = false;
    private int shakeCount = 0;
    private float sensitivityG = 2.2f;
    private int durationSeconds = 10;
    // NaN означає, що датчик ще не дав жодного відліку.
    private float currentGForce = Float.NaN;
    // Текст підказки і ознака завершення таймера переживають поворот екрана.
    private int hintResId = R.string.hint_default;
    private boolean timerFinished = false;

    public ReminderTimer getTimer() {
        return timer;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void setTracking(boolean tracking) {
        isTracking = tracking;
    }

    public int getShakeCount() {
        return shakeCount;
    }

    public void incrementShakeCount() {
        shakeCount++;
    }

    public void setShakeCount(int shakeCount) {
        this.shakeCount = shakeCount;
    }

    public float getSensitivityG() {
        return sensitivityG;
    }

    public void setSensitivityG(float sensitivityG) {
        this.sensitivityG = sensitivityG;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public float getCurrentGForce() {
        return currentGForce;
    }

    public void setCurrentGForce(float currentGForce) {
        this.currentGForce = currentGForce;
    }

    public boolean hasGForce() {
        return !Float.isNaN(currentGForce);
    }

    public int getHintResId() {
        return hintResId;
    }

    public void setHintResId(int hintResId) {
        this.hintResId = hintResId;
    }

    public boolean isTimerFinished() {
        return timerFinished;
    }

    public void setTimerFinished(boolean timerFinished) {
        this.timerFinished = timerFinished;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}
