package com.example.womensafetyapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

        initializeViews();
        loadSavedSettings();
        setupTextWatchers();
    }

    private void initializeViews() {
        contact1Name = findViewById(R.id.contact1Name);
        contact1Phone = findViewById(R.id.contact1Phone);
        contact2Name = findViewById(R.id.contact2Name);
        contact2Phone = findViewById(R.id.contact2Phone);
        contact3Name = findViewById(R.id.contact3Name);
        contact3Phone = findViewById(R.id.contact3Phone);
        emergencyPassword = findViewById(R.id.emergencyPassword);

        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);
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

    private void setupTextWatchers() {
        // Add phone number formatting
        TextWatcher phoneWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString();
                if (!TextUtils.isEmpty(phone) && !phone.matches("^\\d{3}-\\d{3}-\\d{4}$")) {
                    String cleaned = phone.replaceAll("[^\\d]", "");
                    if (cleaned.length() >= 10) {
                        String formatted = cleaned.substring(0, 3) + "-" + 
                                         cleaned.substring(3, 6) + "-" + 
                                         cleaned.substring(6, Math.min(10, cleaned.length()));
                        s.replace(0, s.length(), formatted);
                    }
                }
            }
        };

        contact1Phone.addTextChangedListener(phoneWatcher);
        contact2Phone.addTextChangedListener(phoneWatcher);
        contact3Phone.addTextChangedListener(phoneWatcher);
    }

    public void saveSettings(View view) {
        if (!validateInput()) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("contact1Name", contact1Name.getText().toString().trim());
        editor.putString("contact1Phone", formatPhoneNumber(contact1Phone.getText().toString()));
        editor.putString("contact2Name", contact2Name.getText().toString().trim());
        editor.putString("contact2Phone", formatPhoneNumber(contact2Phone.getText().toString()));
        editor.putString("contact3Name", contact3Name.getText().toString().trim());
        editor.putString("contact3Phone", formatPhoneNumber(contact3Phone.getText().toString()));
        editor.putString("emergencyPassword", emergencyPassword.getText().toString());
        
        if (editor.commit()) {
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(contact1Phone.getText()) &&
            TextUtils.isEmpty(contact2Phone.getText()) &&
            TextUtils.isEmpty(contact3Phone.getText())) {
            Toast.makeText(this, R.string.add_one_contact, Toast.LENGTH_LONG).show();
            return false;
        }

        String password = emergencyPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.set_password, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 4) {
            Toast.makeText(this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validatePhoneNumber(contact1Phone, "Contact 1") ||
            !validatePhoneNumber(contact2Phone, "Contact 2") ||
            !validatePhoneNumber(contact3Phone, "Contact 3")) {
            return false;
        }

        return true;
    }

    private boolean validatePhoneNumber(EditText phoneEdit, String contactName) {
        String phone = phoneEdit.getText().toString();
        if (!TextUtils.isEmpty(phone)) {
            String cleaned = phone.replaceAll("[^\\d]", "");
            if (cleaned.length() < 10) {
                Toast.makeText(this, getString(R.string.invalid_phone) + " - " + contactName, 
                    Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private String formatPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "").trim();
    }
} 