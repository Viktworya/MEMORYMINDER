package com.example.memoryminder;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextAge;
    private Spinner spinnerStage;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private CheckBox checkBoxShowPassword;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        editTextFirstName = findViewById(R.id.etFirstName);
        editTextLastName = findViewById(R.id.etLastName);
        editTextAge = findViewById(R.id.etAge);
        spinnerStage = findViewById(R.id.spinnerStage);
        editTextUsername = findViewById(R.id.etUsername);
        editTextPassword = findViewById(R.id.etPassword);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        Button buttonRegister = findViewById(R.id.btnRegister);

        // Set up the stage dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.stages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStage.setAdapter(adapter);

        // Set click listener for the register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle register button click
                final String firstName = editTextFirstName.getText().toString().trim();
                final String lastName = editTextLastName.getText().toString().trim();
                final int age;
                try {
                    age = Integer.parseInt(editTextAge.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(RegistrationActivity.this, "Invalid age", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String stage = spinnerStage.getSelectedItem().toString();
                final String username = editTextUsername.getText().toString().trim();
                final String password = editTextPassword.getText().toString().trim();

                // Validate names don't contain numbers
                if (firstName.matches(".*\\d.*")) {
                    Toast.makeText(RegistrationActivity.this, "First name cannot contain numbers", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (lastName.matches(".*\\d.*")) {
                    Toast.makeText(RegistrationActivity.this, "Last name cannot contain numbers", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("Patients");

                // Check if username exists
                reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(RegistrationActivity.this, "Username Already exists", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(RegistrationActivity.this, "User Already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Proceed with registration
                                        DatabaseReference newUserRef = reference.child(username);
                                        newUserRef.child("firstName").setValue(firstName);
                                        newUserRef.child("lastName").setValue(lastName);
                                        newUserRef.child("age").setValue(age);
                                        newUserRef.child("stage").setValue(stage);
                                        newUserRef.child("password").setValue(password);
                                        newUserRef.child("username").setValue(username);
                                        Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        finish(); // Return to the previous activity
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(RegistrationActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RegistrationActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        // Show/Hide password functionality
        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            editTextPassword.setSelection(editTextPassword.length()); // Move cursor to the end
        });
    }
}