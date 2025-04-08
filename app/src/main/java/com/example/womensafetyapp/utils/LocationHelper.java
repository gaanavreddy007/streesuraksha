package com.example.womensafetyapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {
    private final Context context;
    private final LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationListener locationListener;
    private final FusedLocationProviderClient fusedLocationClient;
    private SmsManager smsManager;
    private SharedPreferenceHelper sharedPreferenceHelper;

    public interface LocationResultCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.smsManager = SmsManager.getDefault();
        this.sharedPreferenceHelper = new SharedPreferenceHelper(context);
    }

    public void startLocationUpdates(LocationResultCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    callback.onLocationError("Location result is null");
                    return;
                }
                callback.onLocationReceived(locationResult.getLastLocation());
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    public void getCurrentLocation(LocationResultCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                callback.onLocationReceived(lastKnownLocation);
            } else {
                callback.onLocationError("No last known location available");
            }
        } catch (Exception e) {
            callback.onLocationError("Error getting location: " + e.getMessage());
        }
    }

    public void sendEmergencySMS() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String emergencyContacts = sharedPreferenceHelper.getEmergencyContacts();
                            String message = "EMERGENCY! I need help!\nMy location: " +
                                    "https://www.google.com/maps?q=" + location.getLatitude() +
                                    "," + location.getLongitude() +
                                    "\nBattery: " + getBatteryLevel() + "%";

                            if (emergencyContacts != null && !emergencyContacts.isEmpty()) {
                                String[] contacts = emergencyContacts.split(",");
                                for (String number : contacts) {
                                    try {
                                        smsManager.sendTextMessage(number.trim(), null, message, null, null);
                                        Log.d("LocationHelper", "SMS sent to " + number);
                                    } catch (Exception e) {
                                        Log.e("LocationHelper", "Failed to send SMS", e);
                                    }
                                }
                                Toast.makeText(context, "Emergency alert sent!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "No emergency contacts set", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Couldn't get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int getBatteryLevel() {
        // Implement battery level check if needed
        return -1; // Default value if not implemented
    }
}