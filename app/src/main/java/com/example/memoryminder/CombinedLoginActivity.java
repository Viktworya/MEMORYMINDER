package com.example.memoryminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CombinedLoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private CheckBox checkboxShowPassword;
    private Button btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combined_login);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkboxShowPassword = findViewById(R.id.checkboxShowPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        btnAdmin = findViewById(R.id.btnAdmin);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String username = sharedPreferences.getString("username", null);
            String userType = sharedPreferences.getString("userType", null);

            if (username != null && userType != null) {
                if (userType.equals("Doctor")) {
                    Intent intent = new Intent(CombinedLoginActivity.this, NaviActivity.class);
                    intent.putExtra("DOCTOR_USERNAME", username);
                    startActivity(intent);
                    finish();
                } else if (userType.equals("Patient")) {
                    Intent intent = new Intent(CombinedLoginActivity.this, NavPat.class);
                    intent.putExtra("PATIENT_USERNAME", username);
                    startActivity(intent);
                    finish();
                }
            }
        }

        // Set click listener for the login button
        buttonLogin.setOnClickListener(v -> login());

        // Set change listener for the checkbox to show/hide password
        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        // Set click listener for the admin login button
        btnAdmin.setOnClickListener(v -> {
            // Handle admin login button click
            Intent intent = new Intent(CombinedLoginActivity.this, adminlogin.class);
            startActivity(intent);
        });
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(CombinedLoginActivity.this, "Please fill out both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, check in the Doctors database
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors");
        Query doctorQuery = doctorRef.orderByChild("username").equalTo(username);
        doctorQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Doctor doctor = snapshot.getValue(Doctor.class);
                        if (doctor != null && doctor.getPassword().equals(password)) {
                            // Doctor login successful
                            Toast.makeText(CombinedLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Save login state
                            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("username", username);
                            editor.putString("userType", "Doctor");
                            editor.apply();

                            // Redirect to the doctor dashboard and pass the username
                            Intent intent = new Intent(CombinedLoginActivity.this, NaviActivity.class);
                            intent.putExtra("DOCTOR_USERNAME", username);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    // Password is incorrect
                    Toast.makeText(CombinedLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                } else {
                    // If the doctor username does not exist, check in the Patients database
                    checkPatientLogin(username, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle databaseError
                Toast.makeText(CombinedLoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPatientLogin(String username, String password) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Patients");
        Query patientQuery = patientRef.orderByChild("username").equalTo(username);
        patientQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Patient patient = snapshot.getValue(Patient.class);
                        if (patient != null && patient.getPassword().equals(password)) {
                            // Patient login successful
                            Toast.makeText(CombinedLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Save login state
                            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("username", username);
                            editor.putString("userType", "Patient");
                            editor.apply();

                            // Redirect to the patient dashboard and pass the username
                            Intent intent = new Intent(CombinedLoginActivity.this, NavPat.class);
                            intent.putExtra("PATIENT_USERNAME", username);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    // Password is incorrect
                    Toast.makeText(CombinedLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                } else {
                    // Username does not exist in either database
                    Toast.makeText(CombinedLoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle databaseError
                Toast.makeText(CombinedLoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Exit the app when back button is pressed
        finishAffinity(); // This will close all activities and exit the app
    }
}