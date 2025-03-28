package com.example.memoryminder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Modules extends AppCompatActivity {
    private static final int WEEKLY_TARGET_STEPS = 15000;
    private static final int MONTHLY_TARGET_STEPS = 60000;

    private PieChart outerChart; // For Physical Activity
    private PieChart innerChart; // For Cognitive Activity
    private PieChart innermostChart; // For Memory Game Progress
    private DatabaseReference physicalActivityRef;
    private DatabaseReference cognitiveActivityRef;
    private DatabaseReference memoryGameRef;
    private DatabaseReference patientRef;
    private String username;
    private String stage;

    private TextView legendSteps;
    private TextView legendcognitive;
    private TextView legendMemoryGame;
    private Button btnPhysicalActivities, btnGameResults, btnViewReports, btnAnalytics;
    private Button btnWeekly, btnMonthly; // Buttons for weekly and monthly

    private boolean isWeekly = true; // Flag to track the current target (weekly or monthly)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modules);

        outerChart = findViewById(R.id.outerChart);
        innerChart = findViewById(R.id.innerChart);
        innermostChart = findViewById(R.id.innermostChart);
        legendMemoryGame = findViewById(R.id.legendMemoryGame);
        legendSteps = findViewById(R.id.legendSteps);
        legendcognitive = findViewById(R.id.legendcognitive);

        btnWeekly = findViewById(R.id.btnWeekly); // Weekly button
        btnMonthly = findViewById(R.id.btnMonthly); // Monthly button

        // Set up button click listeners
        setupButtonListeners();

        // Get the selected patient's name and username from the intent
        String selectedPatientName = getIntent().getStringExtra("PATIENT_NAME");
        username = getIntent().getStringExtra("PATIENT_USERNAME");

        TextView tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientName.setText(selectedPatientName + " Activity");


        patientRef = FirebaseDatabase.getInstance().getReference("Patients").child(username);
        patientRef.child("stage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stage = dataSnapshot.getValue(String.class);
                TextView tvPatientstage = findViewById(R.id.tvstage);
                tvPatientstage.setText("Stage: " + stage);
                setupFirebaseReferences();
                setChartEmpty(outerChart);
                setChartEmpty(innerChart);
                setChartEmpty(innermostChart);
                fetchPhysicalData();
                fetchCognitiveData();
                fetchMemoryGameData();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to retrieve patient stage: " + databaseError.getMessage());
                Toast.makeText(Modules.this, "Failed to retrieve patient stage", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListeners() {
        btnWeekly.setOnClickListener(v -> {
            isWeekly = true; // Set to weekly
            fetchPhysicalData(); // Refresh data
        });

        btnMonthly.setOnClickListener(v -> {
            isWeekly = false; // Set to monthly
            fetchPhysicalData(); // Refresh data
        });
    }

    private void setupFirebaseReferences() {
        physicalActivityRef = FirebaseDatabase.getInstance().getReference("PatientActivities")
                .child(username).child("Steps");
        cognitiveActivityRef = FirebaseDatabase.getInstance().getReference("GameResults")
                .child(username).child("easy");
        memoryGameRef = FirebaseDatabase.getInstance().getReference("GameResults")
                .child(username).child("MemoryGame");
    }

    private void fetchPhysicalData() {
        physicalActivityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSteps = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String stepsString = snapshot.child("steps").getValue(String.class);
                    if (stepsString != null) {
                        try {
                            int steps = Integer.parseInt(stepsString);
                            totalSteps += steps;
                        } catch (NumberFormatException e) {
                            Log.e("Error", "Invalid step value: " + stepsString);
                        }
                    }
                }

                // Calculate completion percentage based on the target
                int targetSteps = isWeekly ? WEEKLY_TARGET_STEPS : MONTHLY_TARGET_STEPS;
                float completionPercentage = ((float) totalSteps / targetSteps) * 100;

                // Create entries for the PieChart
                List<PieEntry> percentageEntries = new ArrayList<>();
                if (completionPercentage > 0) {
                    percentageEntries.add(new PieEntry(completionPercentage, ""));
                    if (completionPercentage < 100) {
                        percentageEntries.add(new PieEntry(100 - completionPercentage, ""));
                    }
                }

                displayOuterChart(percentageEntries, totalSteps);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to fetch data: " + databaseError.getMessage());
                Toast.makeText(Modules.this, "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCognitiveData() {
        cognitiveActivityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalGames = 0;
                int improvedGames = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String mistakesString = snapshot.child("mistakes").getValue(String.class);
                    if (mistakesString != null) {
                        try {
                            int mistakes = Integer.parseInt(mistakesString);
                            if (mistakes <= 5) {
                                improvedGames++;
                            }
                            totalGames++;
                        } catch (NumberFormatException e) {
                            Log.e("Error", "Invalid mistakes value: " + mistakesString);
                        }
                    }
                }

                float improvedPercentage = (totalGames > 0) ? (improvedGames / (float) totalGames) * 100 : 0;
                List<PieEntry> percentageEntries = new ArrayList<>();
                if (improvedPercentage > 0) {
                    percentageEntries.add(new PieEntry(improvedPercentage, ""));
                    if (improvedPercentage < 100) {
                        percentageEntries.add(new PieEntry(100 - improvedPercentage, ""));
                    }
                }

                displayInnerChart(percentageEntries, improvedPercentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to fetch data: " + databaseError.getMessage());
                Toast.makeText(Modules.this, "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMemoryGameData() {
        memoryGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalGames = 0;
                int improvedGames = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long value = snapshot.child("Time").getValue(Long.class);
                    if (value != null) {
                        if (value <= 120) {
                            improvedGames++;
                        }
                        totalGames++;
                    }
                }

                float improvedPercentage = (totalGames > 0) ? (improvedGames / (float) totalGames) * 100 : 0;
                List<PieEntry> percentageEntries = new ArrayList<>();
                if (improvedPercentage > 0) {
                    percentageEntries.add(new PieEntry(improvedPercentage, ""));
                    if (improvedPercentage < 100) {
                        percentageEntries.add(new PieEntry(100 - improvedPercentage, ""));
                    }
                }

                displayInnermostChart(percentageEntries, improvedPercentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to fetch data: " + databaseError.getMessage());
                Toast.makeText(Modules.this, "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOuterChart(List<PieEntry> entries, int totalSteps) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#f0100c")); // Color for completed steps
        if (entries.size() > 1) {
            colors.add(Color.argb(128, 247, 121, 121)); // Semi-transparent color for remaining steps
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.TRANSPARENT);
        data.setValueFormatter(new PercentFormatter(outerChart));
        outerChart.setData(data);
        outerChart.setHoleRadius(80f);
        outerChart.setTransparentCircleRadius(65f);
        outerChart.getLegend().setEnabled(true);

        // Create custom legend entry for the steps progress
        LegendEntry stepsLegendEntry = new LegendEntry();
        stepsLegendEntry.label = totalSteps + "/" + (isWeekly ? WEEKLY_TARGET_STEPS : MONTHLY_TARGET_STEPS) + " steps";
        stepsLegendEntry.formColor = colors.get(0);

        // Set the custom legend entries
        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(stepsLegendEntry);
        Legend legend = outerChart.getLegend();
        legend.setEnabled(false);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(14f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setCustom(legendEntries);

        // Set custom legend text in the TextView
        legendSteps.setText(stepsLegendEntry.label);
        outerChart.setHoleColor(Color.TRANSPARENT);
        outerChart.getDescription().setEnabled(false);
        outerChart.animateY(1000);
        outerChart.invalidate();
    }

    private void displayInnerChart(List<PieEntry> entries, float improvedPercentage) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#C8FD13")); // Color for improved games
        if (entries.size() > 1) {
            colors.add(Color.argb(128, 220, 233, 175)); // Semi-transparent color for not improved games
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.TRANSPARENT);
        data.setValueFormatter(new PercentFormatter(innerChart));
        innerChart.setData(data);
        innerChart.setHoleRadius(70f);
        innerChart.setTransparentCircleRadius(50f);
        innerChart.getLegend().setEnabled(false);

        // Set custom legend text in the TextView
        String legendText = (entries.isEmpty() ? "Not Recommended" : String.format("%.0f%% Improved", improvedPercentage));
        legendcognitive.setText(legendText);
        innerChart.setHoleColor(Color.TRANSPARENT);
        innerChart.getDescription().setEnabled(false);
        innerChart.animateY(1000);
        innerChart.invalidate();
    }

    private void displayInnermostChart(List<PieEntry> entries, float improvedPercentage) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#f542f5")); // Color for improved games
        if (entries.size() > 1) {
            colors.add(Color.argb(128, 255, 173, 255)); // Semi-transparent color for not improved games
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.TRANSPARENT);
        data.setValueFormatter(new PercentFormatter(innermostChart));
        innermostChart.setData(data);
        innermostChart.setHoleRadius(70f);
        innermostChart.setTransparentCircleRadius(50f);
        innermostChart.getLegend().setEnabled(false);

        // Set custom legend text in the TextView
        String legendText = (entries.isEmpty() ? "Not Recorded" : String.format("%.0f%% Improved", improvedPercentage));
        legendMemoryGame.setText(legendText);
        innermostChart.setHoleColor(Color.TRANSPARENT);
        innermostChart.getDescription().setEnabled(false);
        innermostChart.animateY(1000);
        innermostChart.invalidate();
    }

    private void setChartEmpty(PieChart chart) {
        PieDataSet dataSet = new PieDataSet(new ArrayList<>(), "");
        List<Integer> colors = new ArrayList<>();
        if (chart == outerChart) {
            colors.add(Color.argb(128, 247, 121, 121)); // Semi-transparent red color for the outer chart
        } else if (chart == innerChart) {
            colors.add(Color.argb(128, 220, 233, 175)); // Semi-transparent green color for the inner chart
        } else if (chart == innermostChart) {
            colors.add(Color.argb(128, 243, 134, 252)); // Semi-transparent purple color for the innermost chart
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.TRANSPARENT); // Hide text
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate(); // Refresh the chart
    }
    @Override
    public void onBackPressed() {
        // Instead of restarting NavPat, finish the current activity to go back to the previous fragment
        finish();
    }
}
