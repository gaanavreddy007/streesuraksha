package com.example.womensafetyapp.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

import com.example.womensafetyapp.activities.MainActivity;

public class VolumeButtonAccessibilityService extends AccessibilityService {
    private static final String TAG = "VolumeButtonService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used but required
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected");
        
        // Configure the service
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
        
        Log.d(TAG, "Service configured with key event filtering");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "Key event received: " + event.getKeyCode());
        
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                Log.d(TAG, "Volume button pressed: " + keyCode);
                openApp();
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    private void openApp() {
        try {
            Log.d(TAG, "Attempting to open app");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast.makeText(this, "Women Safety App opened", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "App opened successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error opening app: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening app", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }
} 