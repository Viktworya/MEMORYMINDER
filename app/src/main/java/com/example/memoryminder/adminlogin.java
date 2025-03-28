package com.example.memoryminder;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class adminlogin extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private admindatabase databaseHelper;
    private CheckBox checkBoxShowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminlogin);

        // Instantiate DatabaseHelper
        databaseHelper = new admindatabase(this);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxShowPassword = findViewById(R.id.checkboxShowPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Set click listener for the login button
        buttonLogin.setOnClickListener(v -> {
            // Handle login button click
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            // Authenticate user using the entered credentials
            boolean isAuthenticated = databaseHelper.authenticateUser(username, password);

            if (isAuthenticated) {
                // Navigate to doctor dashboard
                Intent intent = new Intent(adminlogin.this, admindashboard.class);
                startActivity(intent);
                // Finish this activity to prevent user from going back to login screen
            } else {
                // Show error message or handle unsuccessful login
                // For example, you can display a toast message
                Toast.makeText(adminlogin.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Set listener for the show/hide password checkbox
        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Close the database connection when activity is destroyed
        databaseHelper.close();
        super.onDestroy();
    }
}
