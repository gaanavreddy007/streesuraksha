package com.example.womensafetyapp.utils;

import android.location.Location;

/**
 * Interface defining methods that Team Member 2 will implement
 * for voice recognition functionality
 */
public interface VoiceRecognitionListener {
    void startVoiceRecognition();
    void stopVoiceRecognition();
    void onEmergencyWordDetected();
}