package com.example.memoryminder;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalInfo extends AppCompatActivity {

    private DatabaseReference userDatabaseReference;
    private TextView textViewName, textViewFirstName, textViewLastName, textViewAge, textViewUsername, textViewStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Initialize TextViews

        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewAge = findViewById(R.id.textViewAge);
        textViewStage = findViewById(R.id.textViewStage);
        textViewUsername = findViewById(R.id.textViewUsername);

        // Retrieve the username from the Intent
        String username = getIntent().getStringExtra("USERNAME");

        if (username != null) {
            // Initialize Firebase reference
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("Patients");

            // Retrieve and display user information
            userDatabaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Patient patient = dataSnapshot.getValue(Patient.class);

                        if (patient != null) {
                            textViewFirstName.setText("First Name: " + patient.getFirstName());
                            textViewLastName.setText("Last Name: " + patient.getLastName());
                            textViewAge.setText("Age: " + patient.getAge());
                            textViewStage.setText("Stage: " + patient.getStage());
                            textViewUsername.setText("Username: " + username);
                        } else {
                            Toast.makeText(PersonalInfo.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PersonalInfo.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PersonalInfo.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
        }
    }
}
