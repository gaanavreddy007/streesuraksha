package com.example.womensafetyapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.dialogs.EmergencyPasswordDialog;
import com.example.womensafetyapp.utils.EmergencyModeManager;
import com.example.womensafetyapp.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button emergencyButton;
    private Button settingsButton;
    private TextView statusText;
    private SharedPreferences preferences;
    private EmergencyModeManager emergencyModeManager;
    private long pressStartTime;

    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity created");

        initializeViews();
        initializeEmergencyManager();
        checkFirstRun();
        requestPermissions();
        setupButtons();
    }

    private void initializeViews() {
        emergencyButton = findViewById(R.id.emergencyButton);
        settingsButton = findViewById(R.id.settingsButton);
        statusText = findViewById(R.id.statusText);
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);
        Log.d(TAG, "Views initialized");
    }

    private void initializeEmergencyManager() {
        emergencyModeManager = new EmergencyModeManager(this);
        // Start voice recognition immediately if audio permission is granted
        if (PermissionHelper.checkAndRequestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO})) {
            emergencyModeManager.startVoiceRecognition();
            Log.d(TAG, "Voice recognition started");
        }
    }

    private void checkFirstRun() {
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstRun", false);
            editor.putString("emergencyPassword", "1234"); // Default password
            editor.apply();
            Log.d(TAG, "First run setup completed");

            Toast.makeText(this, R.string.setup_contacts, Toast.LENGTH_LONG).show();
            openSettings();
        }
    }

    private void requestPermissions() {
        if (!PermissionHelper.checkAndRequestPermissions(this, REQUIRED_PERMISSIONS)) {
            statusText.setText(R.string.waiting_permissions);
            emergencyButton.setEnabled(false);
            Log.d(TAG, "Waiting for permissions");
        } else {
            emergencyButton.setEnabled(true);
            statusText.setText(R.string.normal_mode);
            Log.d(TAG, "All permissions granted");
        }
    }

    private void setupButtons() {
        // Setup Emergency Button
        emergencyButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressStartTime = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_UP:
                    long pressDuration = System.currentTimeMillis() - pressStartTime;
                    if (pressDuration > 1000) { // 1 second press
                        if (PermissionHelper.checkAndRequestPermissions(this, REQUIRED_PERMISSIONS)) {
                            if (!emergencyModeManager.isEmergencyActive()) {
                                startEmergencyMode();
                            } else {
                                openEmergencyPasswordDialog();
                            }
                        } else {
                            Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_LONG).show();
                        }
                    }
                    return true;
            }
            return false;
        });

        // Setup Settings Button
        settingsButton.setOnClickListener(v -> openSettings());

        // Setup AI Chatbot Button
        Button aiChatbotButton = findViewById(R.id.aiChatbotButton);
        aiChatbotButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatbotActivity.class);
            startActivity(intent);
            Log.d(TAG, "Chatbot activity opened");
        });

        // Setup Map Button
        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> openMap());

        Log.d(TAG, "Buttons setup completed");
    }

    private void startEmergencyMode() {
        emergencyModeManager.startEmergencyMode();
        emergencyButton.setText(R.string.deactivate_emergency);
        statusText.setText(R.string.emergency_active);
        Log.d(TAG, "Emergency mode started");
    }

    private void stopEmergencyMode() {
        emergencyModeManager.stopEmergencyMode();
        emergencyButton.setText(R.string.activate_emergency);
        statusText.setText(R.string.normal_mode);
        // Restart voice recognition after emergency mode is stopped
        if (PermissionHelper.checkAndRequestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO})) {
            emergencyModeManager.startVoiceRecognition();
        }
        Log.d(TAG, "Emergency mode stopped");
    }

    private void openEmergencyPasswordDialog() {
        EmergencyPasswordDialog dialog = new EmergencyPasswordDialog(this, password -> {
            if (password.equals(preferences.getString("emergencyPassword", ""))) {
                stopEmergencyMode();
            } else {
                Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Settings activity opened");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity resumed");
        if (emergencyModeManager.isEmergencyActive()) {
            emergencyButton.setText(R.string.deactivate_emergency);
            statusText.setText(R.string.emergency_active);
        } else {
            emergencyButton.setText(R.string.activate_emergency);
            statusText.setText(R.string.normal_mode);
            // Ensure voice recognition is running in normal mode
            if (PermissionHelper.checkAndRequestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO})) {
                emergencyModeManager.startVoiceRecognition();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emergencyModeManager != null) {
            emergencyModeManager.stopEmergencyMode();
        }
        Log.d(TAG, "MainActivity destroyed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE) {
            if (PermissionHelper.isAllPermissionsGranted(grantResults)) {
                emergencyButton.setEnabled(true);
                statusText.setText(R.string.normal_mode);
                Log.d(TAG, "All permissions granted");
            } else {
                emergencyButton.setEnabled(false);
                statusText.setText(R.string.limited_features);
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Some permissions denied");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Logic to open the app or perform an action
            openApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void openApp() {
        // Logic to open the app or perform an action
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(this, "App opened via volume button", Toast.LENGTH_SHORT).show();
    }

    private void openMap() {
        if (PermissionHelper.checkAndRequestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION})) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            Log.d(TAG, "Map activity opened");
        } else {
            Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
        }
    }
}