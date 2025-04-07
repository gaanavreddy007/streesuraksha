package com.example.womensafetyapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;

public class SettingsActivity extends AppCompatActivity {

    private EditText contact1Name, contact1Phone;
    private EditText contact2Name, contact2Phone;
    private EditText contact3Name, contact3Phone;
    private EditText emergencyPassword;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        contact1Name = findViewById(R.id.contact1Name);
        contact1Phone = findViewById(R.id.contact1Phone);
        contact2Name = findViewById(R.id.contact2Name);
        contact2Phone = findViewById(R.id.contact2Phone);
        contact3Name = findViewById(R.id.contact3Name);
        contact3Phone = findViewById(R.id.contact3Phone);
        emergencyPassword = findViewById(R.id.emergencyPassword);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);

        // Load saved values
        loadSavedSettings();
    }

    private void loadSavedSettings() {
        contact1Name.setText(preferences.getString("contact1Name", ""));
        contact1Phone.setText(preferences.getString("contact1Phone", ""));
        contact2Name.setText(preferences.getString("contact2Name", ""));
        contact2Phone.setText(preferences.getString("contact2Phone", ""));
        contact3Name.setText(preferences.getString("contact3Name", ""));
        contact3Phone.setText(preferences.getString("contact3Phone", ""));
        emergencyPassword.setText(preferences.getString("emergencyPassword", "1234"));
    }

    public void saveSettings(View view) {
        // Validate input
        if (contact1Phone.getText().toString().isEmpty() &&
                contact2Phone.getText().toString().isEmpty() &&
                contact3Phone.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add at least one emergency contact", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emergencyPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please set an emergency password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save all values to SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("contact1Name", contact1Name.getText().toString());
        editor.putString("contact1Phone", contact1Phone.getText().toString());
        editor.putString("contact2Name", contact2Name.getText().toString());
        editor.putString("contact2Phone", contact2Phone.getText().toString());
        editor.putString("contact3Name", contact3Name.getText().toString());
        editor.putString("contact3Phone", contact3Phone.getText().toString());
        editor.putString("emergencyPassword", emergencyPassword.getText().toString());
        editor.apply();

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}