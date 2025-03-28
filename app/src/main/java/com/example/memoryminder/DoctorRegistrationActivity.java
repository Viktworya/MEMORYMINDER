package com.example.memoryminder;

import android.content.Intent;
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
import com.google.firebase.database.ValueEventListener;

public class DoctorRegistrationActivity extends AppCompatActivity {

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextAge;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private CheckBox checkboxShowPassword;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        // Initialize views
        editTextFirstName = findViewById(R.id.FirstName);
        editTextLastName = findViewById(R.id.LastName);
        editTextAge = findViewById(R.id.Age);
        editTextUsername = findViewById(R.id.etUsername);
        editTextPassword = findViewById(R.id.etPassword);
        checkboxShowPassword = findViewById(R.id.checkboxShowPassword); // Initialize the checkbox
        Button buttonRegister = findViewById(R.id.btnRegister);

        // Set click listener for the register button
        buttonRegister.setOnClickListener(v -> {
            final String firstName = editTextFirstName.getText().toString().trim();
            final String lastName = editTextLastName.getText().toString().trim();
            final int age;
            try {
                age = Integer.parseInt(editTextAge.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(DoctorRegistrationActivity.this, "Invalid age", Toast.LENGTH_SHORT).show();
                return;
            }
            final String username = editTextUsername.getText().toString().trim();
            final String password = editTextPassword.getText().toString().trim();

            // Validate first and last names contain only alphabetic characters
            if (!firstName.matches("[a-zA-Z]+")) {
                Toast.makeText(DoctorRegistrationActivity.this, "First name must contain only letters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!lastName.matches("[a-zA-Z]+")) {
                Toast.makeText(DoctorRegistrationActivity.this, "Last name must contain only letters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(DoctorRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Doctors");

            // Check if username exists
            reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(DoctorRegistrationActivity.this, "Username Already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Check for duplicate first and last name with age
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean nameExists = false;

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String existingFirstName = snapshot.child("firstName").getValue(String.class);
                                    String existingLastName = snapshot.child("lastName").getValue(String.class);
                                    Integer existingAge = snapshot.child("age").getValue(Integer.class);

                                    if (existingFirstName != null && existingLastName != null && existingAge != null &&
                                            existingFirstName.equals(firstName) &&
                                            existingLastName.equals(lastName) &&
                                            existingAge == age) {
                                        nameExists = true;
                                        break;
                                    }
                                }

                                if (nameExists) {
                                    Toast.makeText(DoctorRegistrationActivity.this, "Doctor already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Proceed with registration
                                    DatabaseReference newUserRef = reference.child(username);
                                    newUserRef.child("firstName").setValue(firstName);
                                    newUserRef.child("lastName").setValue(lastName);
                                    newUserRef.child("age").setValue(age);
                                    newUserRef.child("password").setValue(password);
                                    newUserRef.child("username").setValue(username);
                                    Toast.makeText(DoctorRegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    navigateToLoginActivity(); // Navigate to the next activity
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(DoctorRegistrationActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DoctorRegistrationActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set change listener for the checkbox to show/hide password
        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Move cursor to the end of the password field
            editTextPassword.setSelection(editTextPassword.length());
        });
    }

    // Helper method to navigate to the login activity
    private void navigateToLoginActivity() {
        Intent intent = new Intent(DoctorRegistrationActivity.this, admindashboard.class);
        startActivity(intent);
        finish(); // Finish this activity to prevent going back
    }
}
