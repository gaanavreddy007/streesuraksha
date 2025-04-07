package com.example.womensafetyapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private Button emergencyButton;
    private Button settingsButton;
    private TextView statusText;
    private boolean isEmergencyMode = false;
    private SharedPreferences preferences;

    // Required permissions
    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        emergencyButton = findViewById(R.id.emergencyButton);
        settingsButton = findViewById(R.id.settingsButton);
        statusText = findViewById(R.id.statusText);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);

        // Request permissions
        if (!PermissionHelper.checkAndRequestPermissions(this, REQUIRED_PERMISSIONS)) {
            statusText.setText("Waiting for permissions...");
        }

        // Check if this is first run, if so, prompt to set up emergency contacts
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstRun", false);
            editor.putString("emergencyPassword", "1234"); // Default password
            editor.apply();

            Toast.makeText(this, "Please set up your emergency contacts and password", Toast.LENGTH_LONG).show();
            openSettings(null);
        }

        // Start the voice recognition service (will be implemented by Team Member 2)
        // Intent voiceServiceIntent = new Intent(this, VoiceService.class);
        // startService(voiceServiceIntent);
    }

    public void toggleEmergencyMode(View view) {
        // Start emergency mode - Team Member 2 will add actual emergency functionality
        Intent intent = new Intent(this, EmergencyActivity.class);
        startActivity(intent);
        finish(); // Close main activity
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // This method would be called by the VoiceService when voice command is detected
    public void activateEmergencyFromVoice() {
        toggleEmergencyMode(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE) {
            if (PermissionHelper.isAllPermissionsGranted(grantResults)) {
                statusText.setText(R.string.normal_mode);
                // Start voice service
                // Intent voiceServiceIntent = new Intent(this, VoiceService.class);
                // startService(voiceServiceIntent);
            } else {
                Toast.makeText(this, "Some permissions were denied. App may not work properly.", Toast.LENGTH_LONG).show();
                statusText.setText("Some features may be limited due to missing permissions");
            }
        }
    }
}