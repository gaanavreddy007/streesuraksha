package com.example.womensafetyapp.activities;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Chronometer;
import android.media.AudioManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;

import java.util.Locale;

public class FakeCallActivity extends AppCompatActivity {
    private static final String TAG = "FakeCallActivity";
    private TextView callerNameText;
    private MediaPlayer mediaPlayer;
    private SharedPreferences preferences;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Chronometer callTimer;
    private AudioManager audioManager;
    private int originalVolume;
    private TextToSpeech textToSpeech;
    private boolean isCallActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);
        Log.d(TAG, "FakeCallActivity created");

        // Keep screen on and show over lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Initialize audio manager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        
        // Set volume to maximum for ringtone
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
            0
        );

        // Initialize views
        callerNameText = findViewById(R.id.callerNameText);
        callTimer = findViewById(R.id.callTimer);

        // Get preferences
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);
        String callerName = preferences.getString("contact1Name", getString(R.string.emergency_contact));
        callerNameText.setText(callerName);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                } else {
                    Log.d(TAG, "TextToSpeech initialized successfully");
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Start ringing
        playRingtone();

        // Auto-answer call after 5 seconds
        handler.postDelayed(this::answerCall, 5000);
    }

    private void playRingtone() {
        try {
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                Log.d(TAG, "Ringtone started");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone", e);
        }
    }

    public void answerCall(View view) {
        answerCall();
    }

    private void answerCall() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "Ringtone stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping ringtone", e);
            }
        }

        // Show the in-call UI
        View ringingLayout = findViewById(R.id.ringingLayout);
        View inCallLayout = findViewById(R.id.inCallLayout);
        
        if (ringingLayout != null) ringingLayout.setVisibility(View.GONE);
        if (inCallLayout != null) inCallLayout.setVisibility(View.VISIBLE);

        // Start call timer
        if (callTimer != null) {
            callTimer.setBase(android.os.SystemClock.elapsedRealtime());
            callTimer.start();
            Log.d(TAG, "Call timer started");
        }

        isCallActive = true;
        startFakeConversation();
    }

    private void startFakeConversation() {
        Log.d(TAG, "Starting fake conversation");
        // Schedule a series of fake conversation messages
        handler.postDelayed(() -> {
            if (isCallActive && textToSpeech != null) {
                textToSpeech.speak("Hello? Are you there?", TextToSpeech.QUEUE_FLUSH, null, null);
                Log.d(TAG, "First message spoken");
            }
        }, 1000);

        handler.postDelayed(() -> {
            if (isCallActive && textToSpeech != null) {
                textToSpeech.speak("I'm on my way to pick you up. Where are you?", TextToSpeech.QUEUE_FLUSH, null, null);
                Log.d(TAG, "Second message spoken");
            }
        }, 5000);

        handler.postDelayed(() -> {
            if (isCallActive && textToSpeech != null) {
                textToSpeech.speak("Okay, I'll be there in 5 minutes. Stay where you are.", TextToSpeech.QUEUE_FLUSH, null, null);
                Log.d(TAG, "Third message spoken");
            }
        }, 10000);

        // End call after 30 seconds
        handler.postDelayed(this::endCall, 30000);
        Log.d(TAG, "Call end scheduled");
    }

    public void endCall(View view) {
        endCall();
    }

    private void endCall() {
        if (!isFinishing()) {
            isCallActive = false;
            cleanupMediaPlayer();
            if (textToSpeech != null) {
                textToSpeech.stop();
            }
            restoreVolume();
            finish();
            Log.d(TAG, "Call ended");
        }
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "MediaPlayer cleaned up");
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up media player", e);
            }
        }
    }

    private void restoreVolume() {
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, originalVolume, 0);
            Log.d(TAG, "Volume restored");
        } catch (Exception e) {
            Log.e(TAG, "Error restoring volume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCallActive = false;
        cleanupMediaPlayer();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        restoreVolume();
        handler.removeCallbacksAndMessages(null);
        if (callTimer != null) {
            callTimer.stop();
        }
        Log.d(TAG, "FakeCallActivity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't stop the fake call when the screen is turned off
        if (!isFinishing()) {
            return;
        }
        cleanupMediaPlayer();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "App opened via volume button", Toast.LENGTH_SHORT).show();
    }
} 