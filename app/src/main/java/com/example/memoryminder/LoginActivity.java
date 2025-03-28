package com.example.memoryminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Realm
        setContentView(R.layout.activity_main);
        Button btnDoctorLogin = findViewById(R.id.btnDoctorLogin);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        btnDoctorLogin.setOnClickListener(v -> {
            // Handle doctor login button click
            Intent intent = new Intent(LoginActivity.this, CombinedLoginActivity.class);
            startActivity(intent);
        });


        btnAdmin.setOnClickListener(v -> {
            // Handle admin login button click
            Intent intent = new Intent(LoginActivity.this, adminlogin.class);
            startActivity(intent);
        });

    }
}
