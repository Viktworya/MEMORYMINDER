package com.example.memoryminder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Walklist extends AppCompatActivity {

    private RecyclerView recyclerViewSteps;
    private StepsAdapter stepsAdapter;
    private List<StepItem> stepItems;
    private DatabaseReference databaseReference;
    private String patientUsername;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walklist);

        recyclerViewSteps = findViewById(R.id.recyclerViewSteps);
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

        // Set up RecyclerView
        stepItems = new ArrayList<>();
        stepsAdapter = new StepsAdapter(this, stepItems, new StepsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String steps, String dataKey) {
                showEditStepsDialog(steps, dataKey);
            }

            @Override
            public void onItemLongClick(String dataKey) {
                showDeleteConfirmationDialog(dataKey);
            }
        });
        recyclerViewSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSteps.setAdapter(stepsAdapter);

        btnSubmitSteps.setOnClickListener(v -> checkAndSubmitSteps());

        // Initialize date formatter
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Load existing steps data for this patient
        loadStepsData();
    }

    private void checkAndSubmitSteps() {
        String todayDate = dateFormat.format(new Date());

        // Check if data for today already exists
        databaseReference.orderByKey().equalTo(todayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Data for today already exists
                    Toast.makeText(Walklist.this, "You have already submitted steps for today.", Toast.LENGTH_SHORT).show();
                } else {
                    // No data for today, allow submission
                    navigateToAddDataActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Walklist.this, "Failed to check today's steps: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAddDataActivity() {
        Intent intent = new Intent(Walklist.this, Walk.class);
        intent.putExtra("PATIENT_USERNAME", patientUsername);
        startActivity(intent);
    }

    private void loadStepsData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stepItems.clear();
                long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // Calculate timestamp for 7 days ago
                List<StepItem> recentSteps = new ArrayList<>();

                for (DataSnapshot dataSnap : snapshot.getChildren()) {
                    String steps = dataSnap.child("steps").getValue(String.class);
                    String date = dataSnap.getKey(); // Assuming date is the key
                    String dataKey = dataSnap.getKey();

                    try {
                        Date stepDate = dateFormat.parse(date);
                        if (stepDate != null && stepDate.getTime() >= sevenDaysAgo) {
                            recentSteps.add(new StepItem(steps, dataKey));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Keep only the last 7 items if needed
                if (recentSteps.size() > 7) {
                    recentSteps = recentSteps.subList(recentSteps.size() - 7, recentSteps.size());
                }

                stepItems.addAll(recentSteps);
                stepsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Walklist.this, "Failed to load steps data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditStepsDialog(String steps, String dataKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Steps");
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_edit_steps, null);

        final EditText inputSteps = viewInflated.findViewById(R.id.dialog_edit_steps);
        inputSteps.setText(steps);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String newSteps = inputSteps.getText().toString().trim();
            if (!newSteps.isEmpty()) {
                updateStepsData(newSteps, dataKey);
            } else {
                Toast.makeText(Walklist.this, "Please enter steps", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateStepsData(String newSteps, String dataKey) {
        databaseReference.child(dataKey).child("steps").setValue(newSteps)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Walklist.this, "Steps data updated successfully", Toast.LENGTH_SHORT).show();
                    loadStepsData(); // Refresh data list
                })
                .addOnFailureListener(e -> Toast.makeText(Walklist.this, "Failed to update steps data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog(String dataKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this data?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteStepsData(dataKey);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteStepsData(String dataKey) {
        databaseReference.child(dataKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Walklist.this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                    loadStepsData(); // Refresh data list
                })
                .addOnFailureListener(e -> Toast.makeText(Walklist.this, "Failed to delete data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
