package com.example.womensafetyapp.activities;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.utils.LocationHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView sourceTextView;
    private Spinner destinationSpinner;
    private Button findButton;
    private LocationHelper locationHelper;
    private GeoPoint userLocation;

    private static final GeoPoint BENGALURU = new GeoPoint(12.9716, 77.5946);
    private static final String[] DESTINATIONS = {
        "MG Road", "Lalbagh Botanical Garden", "Bangalore Palace", "Cubbon Park", "ISKCON Temple"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().load(getApplicationContext(), getPreferences(MODE_PRIVATE));

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(BENGALURU);

        sourceTextView = findViewById(R.id.sourceTextView);
        destinationSpinner = findViewById(R.id.destinationSpinner);
        findButton = findViewById(R.id.findButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DESTINATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(adapter);

        locationHelper = new LocationHelper(this);
        locationHelper.startLocationUpdates(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationReceived(Location location) {
                userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                sourceTextView.setText("Source: " + location.getLatitude() + ", " + location.getLongitude());
                mapView.getController().setCenter(userLocation);
                addMarker(userLocation, "You are here");
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(MapActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        findButton.setOnClickListener(v -> {
            String destination = (String) destinationSpinner.getSelectedItem();
            GeoPoint destinationGeoPoint = getDestinationGeoPoint(destination);
            if (destinationGeoPoint != null && userLocation != null) {
                fetchRoute(userLocation, destinationGeoPoint);
            } else {
                Toast.makeText(MapActivity.this, "Please wait for location or select a valid destination", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private GeoPoint getDestinationGeoPoint(String destination) {
        switch (destination) {
            case "MG Road":
                return new GeoPoint(12.9758, 77.6068);
            case "Lalbagh Botanical Garden":
                return new GeoPoint(12.9507, 77.5848);
            case "Bangalore Palace":
                return new GeoPoint(12.9987, 77.5921);
            case "Cubbon Park":
                return new GeoPoint(12.9763, 77.5929);
            case "ISKCON Temple":
                return new GeoPoint(13.0094, 77.5511);
            default:
                return null;
        }
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
    }

    private void fetchRoute(GeoPoint start, GeoPoint end) {
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + start.getLongitude() + "," + start.getLatitude() + ";"
                + end.getLongitude() + "," + end.getLatitude()
                + "?overview=full&geometries=geojson";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this, "Failed to fetch route", Toast.LENGTH_SHORT).show();
                    e.printStackTrace(); // Log the error
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject geometry = route.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");

                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coord = coordinates.getJSONArray(i);
                                geoPoints.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                            }

                            runOnUiThread(() -> drawRoute(geoPoints));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error parsing route", Toast.LENGTH_SHORT).show());
                        e.printStackTrace(); // Log the error
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this, "Failed to fetch route", Toast.LENGTH_SHORT).show();
                        System.out.println("Response code: " + response.code()); // Log the response code
                    });
                }
            }
        });
    }

    private void drawRoute(List<GeoPoint> geoPoints) {
        Polyline line = new Polyline();
        line.setPoints(geoPoints);
        mapView.getOverlays().add(line);
        mapView.invalidate();
    }

    // ... existing lifecycle methods for mapView ...
}