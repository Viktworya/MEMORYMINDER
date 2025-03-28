package com.example.memoryminder;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class Tracking extends AppCompatActivity {

    private MapView mapView;
    private DatabaseReference databaseReference;
    private String username;
    private Marker userMarker;
    private Handler handler;
    private Runnable updateLocationRunnable;
    private boolean isUserInteracting = false; // Flag to check if user is interacting with the map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this, android.preference.PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.map);

        // Initialize the MapView
        mapView = findViewById(R.id.map);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Get the username from the previous activity
        username = getIntent().getStringExtra("PATIENT_USERNAME");

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocations");

        // Initialize the marker
        userMarker = new Marker(mapView);
        mapView.getOverlays().add(userMarker);

        // Start updating location
        startLocationUpdates();

        // Set initial zoom level and center the map
        mapView.getController().setZoom(20.0); // Set to a higher zoom level
        mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(0, 0)); // Set to a default point (0,0) until updated

        // Listener for user interaction
        mapView.setOnTouchListener((v, event) -> {
            isUserInteracting = true;
            return false;
        });
    }

    private void startLocationUpdates() {
        handler = new Handler();
        updateLocationRunnable = new Runnable() {
            @Override
            public void run() {
                getLocationFromFirebase(username);
                    handler.postDelayed(this, 1000); // Update every 10 seconds
            }
        };
        handler.post(updateLocationRunnable);
    }

    private void getLocationFromFirebase(String username) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        // Update the marker position
                        userMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                        userMarker.setTitle("User Location");
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.invalidate(); // Refresh the map

                        // Center the map on the new location only if the user is not interacting
                        if (!isUserInteracting) {
                            mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
                        }
                    }
                } else {
                    Log.d("Firebase", "No such user!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error getting user location: " + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        handler.removeCallbacks(updateLocationRunnable); // Stop updates when paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
        handler.removeCallbacks(updateLocationRunnable); // Stop updates when destroyed
    }
}