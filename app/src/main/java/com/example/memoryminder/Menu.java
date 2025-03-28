package com.example.memoryminder; // Change this to your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memorymenu); // Ensure this matches your layout file name

        // Retrieve the username passed from the previous activity
        Intent intent = getIntent();
        loggedInUsername = intent.getStringExtra("PATIENT_USERNAME");

        // Initialize buttons
        Button easyButton = findViewById(R.id.easyButton);
        Button mediumButton = findViewById(R.id.mediumButton);

        // Set click listener for the easy button
        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the easy game activity
                Intent intent = new Intent(Menu.this, MemoryGameModerate.class); // Replace with your actual game activity
                intent.putExtra("PATIENT_USERNAME", loggedInUsername);
                startActivity(intent);
            }
        });

        // Set click listener for the medium button
        mediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the medium game activity
                Intent intent = new Intent(Menu.this, MemoryGameSevere.class); // Replace with your actual game activity
                intent.putExtra("PATIENT_USERNAME", loggedInUsername);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Optionally handle the back button press
        super.onBackPressed();
    }
}