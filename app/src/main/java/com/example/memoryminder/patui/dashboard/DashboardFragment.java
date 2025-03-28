package com.example.memoryminder.patui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoryminder.MainActivity;
import com.example.memoryminder.Menu;
import com.example.memoryminder.R;
import com.example.memoryminder.SharedViewModel;
import com.example.memoryminder.Tracking;
import com.example.memoryminder.Walklist;
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

public class DashboardFragment extends Fragment {
    private static final int WEEKLY_TARGET_STEPS = 15000;
    private static final int MONTHLY_TARGET_STEPS = 60000;

    private PieChart outerChart; // For Physical Activity
    private PieChart innerChart; // For Cognitive Activity
    private PieChart innermostChart; // For Memory Game Progress
    private DatabaseReference physicalActivityRef;
    private DatabaseReference cognitiveActivityRef;
    private DatabaseReference memoryGameRef;
    private String username;
    private String stage;

    private TextView legendSteps;
    private TextView legendcognitive;
    private TextView legendMemoryGame;
    private Button btnPhysicalActivities, btnGameResults, btnViewReports, btnAnalytics;
    private Button btnWeekly, btnMonthly; // Buttons for weekly and monthly

    private boolean isWeekly = true; // Flag to track the current target (weekly or monthly)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize the PieCharts
        outerChart = view.findViewById(R.id.outerChart);
        innerChart = view.findViewById(R.id.innerChart);
        innermostChart = view.findViewById(R.id.innermostChart);
        legendMemoryGame = view.findViewById(R.id.legendMemoryGame);
        legendSteps = view.findViewById(R.id.legendSteps);
        legendcognitive = view.findViewById(R.id.legendcognitive);

        // Initialize the Buttons
        btnPhysicalActivities = view.findViewById(R.id.btnWalk);
        btnGameResults = view.findViewById(R.id.btnJog);
        btnViewReports = view.findViewById(R.id.btnSudoku);
        btnAnalytics = view.findViewById(R.id.btnSample);
        btnWeekly = view.findViewById(R.id.btnWeekly); // Weekly button
        btnMonthly = view.findViewById(R.id.btnMonthly); // Monthly button

        // Set up button click listeners
        setupButtonListeners();

        // Retrieve the SharedViewModel
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String retrievedUsername) {
                username = retrievedUsername;
                if (username != null) {
                    setupFirebaseReferences();
                    fetchPhysicalData();
                    fetchCognitiveData();
                    fetchMemoryGameData();
                }
            }
        });

        // Set initial transparent state for charts
        setChartEmpty(outerChart);
        setChartEmpty(innerChart);
        setChartEmpty(innermostChart);

        return view;
    }

    private void setupButtonListeners() {
        btnPhysicalActivities.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Walklist.class);
            intent.putExtra("PATIENT_USERNAME", username);
            startActivity(intent);
        });

        btnGameResults.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Tracking.class);
            intent.putExtra("PATIENT_USERNAME", username);
            startActivity(intent);
        });

        btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("PATIENT_USERNAME", username);
            startActivity(intent);
        });

        btnAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Menu.class);
            intent.putExtra("PATIENT_USERNAME", username);
            startActivity(intent);
        });

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
                Toast.makeText(getContext(), "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
        outerChart.animateY(1000);
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
        outerChart.animateY(1000);
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
    private void updateChartVisibility(String stage) {
        if ("Mild".equals(stage)) {
            innermostChart.setVisibility(View.GONE); // Hide innermost chart for Mild stage
            innerChart.setVisibility(View.VISIBLE); // Show inner chart
        } else {
            innermostChart.setVisibility(View.VISIBLE); // Show innermost chart for Moderate or Severe
            innerChart.setVisibility(View.GONE); // Hide inner chart
        }
    }
}