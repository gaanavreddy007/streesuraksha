package com.example.womensafetyapp.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class SMSHelper {
    private final Context context;
    private final SmsManager smsManager;

    public interface SMSCallback {
        void onResult(boolean success);
    }

    public SMSHelper(Context context) {
        this.context = context;
        this.smsManager = SmsManager.getDefault();
    }

    public void sendEmergencySMS(String phoneNumber, String message, SMSCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission required", Toast.LENGTH_SHORT).show();
            callback.onResult(false);
            return;
        }

        try {
            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                    new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);

            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
            callback.onResult(true);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onResult(false);
        }
    }
} 