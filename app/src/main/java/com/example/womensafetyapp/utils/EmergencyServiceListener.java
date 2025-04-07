package com.example.womensafetyapp.utils;

import android.location.Location;

/**
 * Interface defining methods that Team Member 2 will implement
 * for emergency services functionality
 */
public interface EmergencyServiceListener {
    void sendEmergencySMS(String[] phoneNumbers, Location location);
    void getCurrentLocation(LocationCallback callback);

    interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String errorMessage);
    }
}