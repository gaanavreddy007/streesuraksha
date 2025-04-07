package com.example.womensafetyapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.utils.EmergencyModeManager;

import java.util.ArrayList;

public class VoiceRecognitionService extends Service implements RecognitionListener {
    private static final String TAG = "VoiceRecognitionService";
    private SpeechRecognizer speechRecognizer;
    private EmergencyModeManager emergencyModeManager;
    private boolean isListening = false;
    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                speechRecognizer.setRecognitionListener(this);
                emergencyModeManager = new EmergencyModeManager(this);
                Log.d(TAG, "Voice recognition service created successfully");
                Toast.makeText(this, "Voice recognition service started", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Speech recognition is not available on this device");
                Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating speech recognizer", e);
            Toast.makeText(this, "Error starting voice recognition: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting voice recognition service");
        startListening();
        return START_STICKY;
    }

    private void startListening() {
        if (!isListening && speechRecognizer != null) {
            try {
                isListening = true;
                Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
                
                speechRecognizer.startListening(recognizerIntent);
                Log.d(TAG, "Started listening for voice commands");
                Toast.makeText(this, "Listening for voice commands...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error starting voice recognition", e);
                Toast.makeText(this, "Error starting voice recognition: " + e.getMessage(), Toast.LENGTH_LONG).show();
                handleError();
            }
        }
    }

    @Override
    public void onResults(Bundle results) {
        if (results != null) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String text = matches.get(0).toLowerCase();
                Log.d(TAG, "Recognized text: " + text);
                Toast.makeText(this, "Heard: " + text, Toast.LENGTH_SHORT).show();
                
                if (text.contains("help") || text.contains("emergency")) {
                    Log.d(TAG, "Emergency command detected");
                    if (!emergencyModeManager.isEmergencyActive()) {
                        emergencyModeManager.startEmergencyMode();
                        Toast.makeText(this, "Emergency mode activated by voice command", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.d(TAG, "No matches found in recognition results");
                Toast.makeText(this, "No speech detected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Null results received");
            Toast.makeText(this, "No recognition results", Toast.LENGTH_SHORT).show();
        }
        retryCount = 0; // Reset retry count on successful recognition
        startListening();
    }

    @Override
    public void onError(int error) {
        Log.e(TAG, "Voice recognition error: " + error);
        isListening = false;
        
        String errorMessage = "Voice recognition error: ";
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorMessage += "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorMessage += "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMessage += "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage += "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorMessage += "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorMessage += "No match found";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage += "Recognition service busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage += "Server error";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorMessage += "No speech input";
                break;
            default:
                errorMessage += "Unknown error";
        }
        
        Log.e(TAG, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        if (error != SpeechRecognizer.ERROR_NO_MATCH) {
            handleError();
        } else {
            startListening();
        }
    }

    private void handleError() {
        retryCount++;
        if (retryCount < MAX_RETRIES) {
            Log.d(TAG, "Retrying voice recognition, attempt " + retryCount);
            Toast.makeText(this, "Retrying voice recognition...", Toast.LENGTH_SHORT).show();
            new android.os.Handler(getMainLooper()).postDelayed(this::startListening, 1000);
        } else {
            Log.e(TAG, "Max retries reached, stopping service");
            Toast.makeText(this, "Voice recognition failed after multiple attempts", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        Log.d(TAG, "Voice recognition service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        isListening = true;
        Log.d(TAG, "Ready for speech");
        Toast.makeText(this, "Ready for speech", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech detected");
        Toast.makeText(this, "Speech detected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Not used
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Not used
    }

    @Override
    public void onEndOfSpeech() {
        isListening = false;
        Log.d(TAG, "End of speech detected");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // Not used
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // Not used
    }
} 