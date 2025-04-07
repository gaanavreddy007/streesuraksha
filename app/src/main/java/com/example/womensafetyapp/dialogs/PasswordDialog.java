package com.example.womensafetyapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.womensafetyapp.R;

public class PasswordDialog {
    private final Context context;
    private final PasswordCallback callback;
    private final SharedPreferences preferences;

    public interface PasswordCallback {
        void onPasswordResult(boolean success);
    }

    public PasswordDialog(Context context, PasswordCallback callback) {
        this.context = context;
        this.callback = callback;
        this.preferences = context.getSharedPreferences("WomenSafetyPrefs", Context.MODE_PRIVATE);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.enter_password);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_password, null);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPassword = passwordInput.getText().toString();
            String savedPassword = preferences.getString("emergencyPassword", "1234");
            
            boolean isCorrect = enteredPassword.equals(savedPassword);
            if (!isCorrect) {
                Toast.makeText(context, R.string.incorrect_password, Toast.LENGTH_SHORT).show();
            }
            callback.onPasswordResult(isCorrect);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
} 