package com.example.womensafetyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {
    private static final String PREFS_NAME = "WomenSafetyPrefs";
    private static final String KEY_EMERGENCY_CONTACTS = "emergency_contacts";
    private static final String KEY_EMERGENCY_PASSWORD = "emergency_password";

    private SharedPreferences sharedPreferences;

    public SharedPreferenceHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setEmergencyContacts(String contacts) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_CONTACTS, contacts).apply();
    }

    public String getEmergencyContacts() {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACTS, "");
    }

    public void setEmergencyPassword(String password) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_PASSWORD, password).apply();
    }

    public boolean checkPassword(String inputPassword) {
        String savedPassword = sharedPreferences.getString(KEY_EMERGENCY_PASSWORD, "safepass123");
        return savedPassword.equals(inputPassword);
    }
}