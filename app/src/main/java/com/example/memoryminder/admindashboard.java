package com.example.memoryminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class admindashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);

        Button btnDoctorLogin = findViewById(R.id.btnadmin);
        Button btnDoctorLogout = findViewById(R.id.btnLogout);

        btnDoctorLogin.setOnClickListener(v -> {
            // Handle doctor login button click
            Intent intent = new Intent(admindashboard.this, DoctorRegistrationActivity.class);
            startActivity(intent);
        });

        btnDoctorLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    @Override
    public void onBackPressed() {
        // Show logout confirmation dialog when the back button is pressed
        showLogoutConfirmation();
        // Call super.onBackPressed() after showing the dialog
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Logout
                    Intent intent = new Intent(admindashboard.this, CombinedLoginActivity.class);
                    startActivity(intent);
                    // Call super.onBackPressed() after starting the LoginActivity
                    super.onBackPressed();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // User clicked the "No" button, so dismiss the dialog
                    dialog.dismiss();
                });
        // Create the AlertDialog object and show it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}