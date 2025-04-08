package com.example.womensafetyapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.utils.LocationHelper;
import com.example.womensafetyapp.utils.SMSHelper;
import com.example.womensafetyapp.dialogs.PasswordDialog;

public class EmergencyActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private TextView locationStatus, smsStatus;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationHelper locationHelper;
    private SMSHelper smsHelper;
    private boolean isEmergencyActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        initializeComponents();
        setupWindow();
        startEmergencyProcedures();
    }

    private void initializeComponents() {
        locationStatus = findViewById(R.id.locationStatus);
        smsStatus = findViewById(R.id.smsStatus);
        locationHelper = new LocationHelper(this);
        smsHelper = new SMSHelper(this);
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);
    }

    private void setupWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void startEmergencyProcedures() {
        String[] phoneNumbers = new String[3];
        phoneNumbers[0] = preferences.getString("contact1Phone", "");
        phoneNumbers[1] = preferences.getString("contact2Phone", "");
        phoneNumbers[2] = preferences.getString("contact3Phone", "");

        locationHelper.startLocationUpdates(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (isEmergencyActive) {
                    String locationUrl = "https://maps.google.com/?q=" + 
                        location.getLatitude() + "," + location.getLongitude();
                    String message = "EMERGENCY! I need help! My location: " + locationUrl;
                    
                    for (String phone : phoneNumbers) {
                        if (phone != null && !phone.isEmpty()) {
                            smsHelper.sendEmergencySMS(phone, message, success -> {
                                runOnUiThread(() -> {
                                    smsStatus.setText(success ? 
                                        R.string.sms_sent : R.string.sms_error);
                                });
                            });
                        }
                    }
                }
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> locationStatus.setText(R.string.location_error));
            }
        });

        schedulePeriodicUpdates(phoneNumbers);
    }

    private void schedulePeriodicUpdates(String[] phoneNumbers) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isEmergencyActive) {
                    startEmergencyProcedures();
                    handler.postDelayed(this, 300000); // Update every 5 minutes
                }
            }
        }, 300000);
    }

    public void showPasswordDialog(View view) {
        new PasswordDialog(this, success -> {
            if (success) {
                deactivateEmergency();
            }
        }).show();
    }

    private void deactivateEmergency() {
        isEmergencyActive = false;
        locationHelper.stopLocationUpdates();
        handler.removeCallbacksAndMessages(null);
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        showPasswordDialog(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isEmergencyActive = false;
        locationHelper.stopLocationUpdates();
        handler.removeCallbacksAndMessages(null);
    }
} 