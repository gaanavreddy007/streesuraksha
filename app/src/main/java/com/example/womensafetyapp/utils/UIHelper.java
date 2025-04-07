package com.example.womensafetyapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.womensafetyapp.R;

/**
 * Helper class for UI operations
 */
public class UIHelper {

    private static AlertDialog progressDialog;

    /**
     * Show a loading dialog
     */
    public static void showLoading(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        TextView loadingText = dialogView.findViewById(R.id.loadingText);
        loadingText.setText(message);

        builder.setView(dialogView);
        builder.setCancelable(false);

        progressDialog = builder.create();
        progressDialog.show();
    }

    /**
     * Hide the loading dialog
     */
    public static void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Show a simple alert dialog
     */
    public static void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}