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

public class PersonalInfoActivity extends AppCompatActivity {

    private DatabaseReference userDatabaseReference;
    private TextView textViewName, textViewFirstName, textViewLastName, textViewAge, textViewUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_personal);

        // Initialize TextViews

        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewAge = findViewById(R.id.textViewAge);
        textViewUsername = findViewById(R.id.textViewUsername);

        // Retrieve the username from the Intent
        String username = getIntent().getStringExtra("USERNAME");

        if (username != null) {
            // Initialize Firebase reference
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("Doctors");

            // Retrieve and display user information
            userDatabaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Doctor doctor = dataSnapshot.getValue(Doctor.class);
                        // Display doctor's information
                        if (doctor != null) {
                            textViewFirstName.setText("First Name: " + doctor.getFirstName());
                            textViewLastName.setText("Last Name: " + doctor.getLastName());
                            textViewAge.setText("Age: " + doctor.getAge());
                            textViewUsername.setText("Username: " + username);
                        } else {
                            Toast.makeText(PersonalInfoActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PersonalInfoActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PersonalInfoActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
        }
    }
}
