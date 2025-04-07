package com.example.womensafetyapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.activities.EmergencyActivity;

public class EmergencyServiceListener {
    private final Context context;
    private final Handler handler;
    private final SharedPreferences preferences;
    private boolean isEmergencyActive = false;

    public EmergencyServiceListener(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.preferences = context.getSharedPreferences("WomenSafetyPrefs", Context.MODE_PRIVATE);
    }

    public void startEmergency() {
        if (!isEmergencyActive) {
            isEmergencyActive = true;
            Intent intent = new Intent(context, EmergencyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void stopEmergency() {
        isEmergencyActive = false;
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isEmergencyActive() {
        return isEmergencyActive;
    }
}
