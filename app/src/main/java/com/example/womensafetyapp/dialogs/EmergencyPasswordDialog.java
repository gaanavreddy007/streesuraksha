package com.example.womensafetyapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.womensafetyapp.R;

public class EmergencyPasswordDialog extends Dialog {
    private final OnPasswordEnteredListener listener;
    private EditText passwordEditText;

    public interface OnPasswordEnteredListener {
        void onPasswordEntered(String password);
    }

    public EmergencyPasswordDialog(@NonNull Context context, OnPasswordEnteredListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_emergency_password);

        passwordEditText = findViewById(R.id.passwordEditText);
        Button submitButton = findViewById(R.id.submitButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        submitButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            if (!password.isEmpty()) {
                listener.onPasswordEntered(password);
                dismiss();
            } else {
                Toast.makeText(getContext(), R.string.enter_password, Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
} 