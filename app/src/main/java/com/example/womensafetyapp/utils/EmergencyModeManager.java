package com.example.womensafetyapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.activities.EmergencyActivity;
import com.example.womensafetyapp.activities.FakeCallActivity;
import com.example.womensafetyapp.services.VoiceRecognitionService;

public class EmergencyModeManager {
    private static final String TAG = "EmergencyModeManager";
    private final Context context;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final SharedPreferences preferences;
    private final Handler handler;
    private boolean isEmergencyActive = false;
    private float lastX, lastY, lastZ;
    private static final float SHAKE_THRESHOLD = 15.0f;
    private long lastUpdate = 0;
    private static final int SHAKE_INTERVAL = 1000;
    private boolean isShakeDetectionActive = false;
    private boolean isVoiceRecognitionActive = false;

    public EmergencyModeManager(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.preferences = context.getSharedPreferences("WomenSafetyPrefs", Context.MODE_PRIVATE);
        this.handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "EmergencyModeManager initialized");
    }

    public void startEmergencyMode() {
        if (!isEmergencyActive) {
            Log.d(TAG, "Starting emergency mode");
            isEmergencyActive = true;
            startShakeDetection();
            Toast.makeText(context, R.string.emergency_active, Toast.LENGTH_SHORT).show();
            
            // Start EmergencyActivity
            Intent intent = new Intent(context, EmergencyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void stopEmergencyMode() {
        if (isEmergencyActive) {
            Log.d(TAG, "Stopping emergency mode");
            isEmergencyActive = false;
            stopShakeDetection();
            Toast.makeText(context, R.string.normal_mode, Toast.LENGTH_SHORT).show();
        }
    }

    public void startVoiceRecognition() {
        if (!isVoiceRecognitionActive) {
            try {
                Log.d(TAG, "Starting voice recognition");
                Intent serviceIntent = new Intent(context, VoiceRecognitionService.class);
                context.startService(serviceIntent);
                isVoiceRecognitionActive = true;
            } catch (Exception e) {
                Log.e(TAG, "Error starting voice recognition", e);
                Toast.makeText(context, R.string.voice_recognition_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void stopVoiceRecognition() {
        if (isVoiceRecognitionActive) {
            try {
                Log.d(TAG, "Stopping voice recognition");
                Intent serviceIntent = new Intent(context, VoiceRecognitionService.class);
                context.stopService(serviceIntent);
                isVoiceRecognitionActive = false;
            } catch (Exception e) {
                Log.e(TAG, "Error stopping voice recognition", e);
                Toast.makeText(context, R.string.voice_recognition_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startShakeDetection() {
        if (accelerometer != null && !isShakeDetectionActive) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            isShakeDetectionActive = true;
            Log.d(TAG, "Started shake detection");
        }
    }

    private void stopShakeDetection() {
        if (isShakeDetectionActive) {
            sensorManager.unregisterListener(sensorListener);
            isShakeDetectionActive = false;
            Log.d(TAG, "Stopped shake detection");
        }
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > SHAKE_INTERVAL) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        Log.d(TAG, "Shake detected with speed: " + speed);
                        handler.post(() -> {
                            if (!isEmergencyActive) {
                                startEmergencyMode();
                            } else {
                                triggerFakeCall();
                            }
                        });
                    }

                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed
        }
    };

    private void triggerFakeCall() {
        Log.d(TAG, "Triggering fake call");
        Intent intent = new Intent(context, FakeCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isEmergencyActive() {
        return isEmergencyActive;
    }
}