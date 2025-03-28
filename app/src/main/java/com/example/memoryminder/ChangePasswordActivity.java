package com.example.memoryminder;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextOldPassword, editTextNewPassword, editTextConfirmNewPassword;
    private CheckBox checkBoxShowHidePassword;
    private Button buttonSubmitPasswordChange, buttonCancelPasswordChange;

    private String username;
    private String storedPassword;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword);
        checkBoxShowHidePassword = findViewById(R.id.checkBoxShowHidePassword);
        buttonSubmitPasswordChange = findViewById(R.id.buttonSubmitPasswordChange);
        buttonCancelPasswordChange = findViewById(R.id.buttonCancelPasswordChange);

        // Get the username from the Intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "Username is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if username is null
            return;
        }

        reference = FirebaseDatabase.getInstance().getReference("Doctors");

        // Retrieve the current stored password from Firebase
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        storedPassword = doctor.getPassword(); // Store the password for verification
                        if (storedPassword == null) {
                            Toast.makeText(ChangePasswordActivity.this, "Password data not available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Doctor data is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Doctor does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChangePasswordActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle showing/hiding passwords
        checkBoxShowHidePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                editTextOldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                editTextNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                editTextConfirmNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                editTextOldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editTextNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editTextConfirmNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        buttonSubmitPasswordChange.setOnClickListener(v -> changePassword());

        buttonCancelPasswordChange.setOnClickListener(v -> finish()); // Cancel and close the activity
    }

    private void changePassword() {
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            editTextOldPassword.setError("Enter your old password");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("Enter your new password");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmNewPassword.setError("Confirm your new password");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmNewPassword.setError("Passwords do not match");
            return;
        }

        // Ensure storedPassword is not null
        if (storedPassword == null) {
            Toast.makeText(this, "Unable to verify old password. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify old password
        if (!oldPassword.equals(storedPassword)) {
            editTextOldPassword.setError("Incorrect old password");
            return;
        }
        if (newPassword.equals(storedPassword)) {
            editTextNewPassword.setError("New password cannot be the same as the old password");
            return;
        }

        // Update password in Firebase
        reference.child(username).child("password").setValue(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after successful password change
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
