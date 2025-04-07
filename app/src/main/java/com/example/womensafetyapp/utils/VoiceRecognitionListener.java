package com.example.womensafetyapp.utils;

public interface VoiceRecognitionListener {
    void onVoiceCommandDetected(String command);
    void onVoiceRecognitionError(String error);
}