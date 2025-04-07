package com.example.womensafetyapp.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD = 2.7f; // Reduced threshold for better sensitivity
    private static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    private OnShakeListener listener;
    private long lastShakeTime;
    private boolean isEnabled = true;

    public interface OnShakeListener {
        void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isEnabled || listener == null || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        try {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z)
                    - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
                    lastShakeTime = currentTime;
                    listener.onShake();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
} 