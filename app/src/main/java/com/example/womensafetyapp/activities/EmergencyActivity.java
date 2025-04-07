package com.example.womensafetyapp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;

public class EmergencyActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("WomenSafetyPrefs", MODE_PRIVATE);

        // Make activity fullscreen and prevent screen from turning off
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // The actual location sharing and SMS sending will be handled by Team Member 2
        // This is where we'd call those functions
    }

    // Show the password dialog when trying to exit emergency mode
    public void showPasswordDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enter_password));

        // Inflate and set the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
        builder.setView(dialogView);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            checkPassword(passwordInput.getText().toString());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Check the entered password against the saved password
    private void checkPassword(String enteredPassword) {
        String savedPassword = preferences.getString("emergencyPassword", "1234");

        if (enteredPassword.equals(savedPassword)) {
            // Password correct, deactivate emergency mode
            deactivateEmergency();
        } else {
            // Password incorrect
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
        }
    }

    // Deactivate emergency and return to the main activity
    private void deactivateEmergency() {
        // Return to main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Disable the back button to prevent the user from easily exiting emergency mode
    @Override
    public void onBackPressed() {
        // Instead of allowing the back press to go through, show the password dialog
        showPasswordDialog(null);
    }
}
