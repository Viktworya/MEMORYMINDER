package com.example.memoryminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Walk extends AppCompatActivity {

    private EditText etSteps;
    private DatabaseReference databaseReference;
    private String patientUsername; // Patient username to be passed from previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walk);

        etSteps = findViewById(R.id.etSteps);
        Button btnSubmitSteps = findViewById(R.id.btnSubmitSteps);

        // Get the patient username from the Intent
        patientUsername = getIntent().getStringExtra("PATIENT_USERNAME");
        if (patientUsername == null) {
            // Handle the case where patient username is not passed
            Toast.makeText(this, "No patient username provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("PatientActivities").child(patientUsername).child("Steps");

        btnSubmitSteps.setOnClickListener(v -> {
            String steps = etSteps.getText().toString().trim();
            if (steps.isEmpty()) {
                Toast.makeText(Walk.this, "Please enter steps", Toast.LENGTH_SHORT).show();
            } else {
                checkAndSubmitStepsData(steps);
            }
        });
    }

    @Override
    public void onBackPressed() {
        showCancelConfirmationDialog();
    }

    private void checkAndSubmitStepsData(String steps) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if there's already an entry for the current date
        databaseReference.orderByChild("timestamp").startAt(currentDate).endAt(currentDate + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If an entry exists for the current date, notify the user
                    Toast.makeText(Walk.this, "You can only submit steps once a day.", Toast.LENGTH_SHORT).show();
                } else {
                    // No entry exists for today, so submit the data
                    submitStepsData(steps);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Walk.this, "Error checking for existing data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitStepsData(String steps) {
        String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        DatabaseReference newEntry = databaseReference.child(currentDateAndTime.replace(":", "_").replace(" ", "_"));
        newEntry.child("steps").setValue(steps);
        newEntry.child("timestamp").setValue(currentDateAndTime);

        Toast.makeText(this, "Steps data submitted", Toast.LENGTH_SHORT).show();
        etSteps.setText("");

        // Navigate back to the Walklist activity
        Intent intent = new Intent(Walk.this, Walklist.class);
        intent.putExtra("PATIENT_USERNAME", patientUsername);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the back stack to avoid loop
        startActivity(intent);
        finish(); // Finish this activity so it's not on the back stack
    }

    private void showCancelConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to cancel submitting steps?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finish(); // Finish this activity so it's not on the back stack
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Do nothing, stay on this activity
                    dialog.dismiss();
                });
        builder.create().show();
    }
}
