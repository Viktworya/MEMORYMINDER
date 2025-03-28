package com.example.memoryminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameOverActivityModerate extends AppCompatActivity {

    private TextView finalScoreTextView;
    private DatabaseReference database;
    private String loggedInUsername;
    private int finalTimeInSeconds; // Store the time spent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        finalScoreTextView = findViewById(R.id.finalScoreTextView);

        // Retrieve the time spent from the previous activity
        finalTimeInSeconds = getIntent().getIntExtra("FINAL_SCORE", 0);

        // Convert the time from seconds to MM:SS format
        String timeSpentFormatted = formatTime(finalTimeInSeconds);

        // Display the time spent in the TextView
        finalScoreTextView.setText("Time: " + timeSpentFormatted);

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference();

        // Get the logged-in user's username from the intent
        loggedInUsername = getIntent().getStringExtra("PATIENT_USERNAME");

        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            loggedInUsername = "Unknown"; // Fallback in case username is null
        }

        // Check the patient's stage and save the game results
        checkPatientStageAndSaveResults();
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void checkPatientStageAndSaveResults() {
        // Reference to the patient's stage
        DatabaseReference patientStageRef = database.child("Patients").child(loggedInUsername).child("stage");

        patientStageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String patientStage = dataSnapshot.getValue(String.class);
                    Log.d("GameOverActivity", "Patient stage: " + patientStage);


                    if ("Moderate".equalsIgnoreCase(patientStage)) {
                        saveGameResults("Moderate");
                    } else {
                        Log.d("GameOverActivity", "Stage is not Severe: " + patientStage);
                    }
                } else {
                    Log.e("GameOverActivity", "Patient stage not found for user: " + loggedInUsername);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GameOverActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void saveGameResults(String stage) {
        // Create a formatted date string for now
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        // Save the time spent under the user's node in Firebase
        database.child("GameResults")
                .child(loggedInUsername)
                .child("MemoryGame")
                .child(currentDateTime)
                .child("Time")
                .setValue(finalTimeInSeconds)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("GameOverActivity", "Time saved to Firebase (in seconds) for user: " + loggedInUsername + " at " + currentDateTime);
                    } else {
                        Log.e("GameOverActivity", "Failed to save time to Firebase");
                    }
                });
    }

    public void menu(View view) {
        // Navigate back to the DashboardFragment in NavPat activity
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack("DashboardFragment", 0);
        } else {
            // If not found, finish this activity (fallback)
            finish();
        }
    }

    public void again(View view) {
        // Restart the game (assuming a similar game activity)
        Intent intent = new Intent(GameOverActivityModerate.this, MemoryGameSevere.class); // Replace with your actual game activity
        intent.putExtra("PATIENT_USERNAME", loggedInUsername);
        startActivity(intent);
        finish();
    }
}