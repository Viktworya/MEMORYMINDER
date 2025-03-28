package com.example.memoryminder.patui.profile;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoryminder.AboutActivity;
import com.example.memoryminder.ChangePassword;
import com.example.memoryminder.CombinedLoginActivity;
import com.example.memoryminder.LoginActivity;
import com.example.memoryminder.Patient;
import com.example.memoryminder.PersonalInfo;
import com.example.memoryminder.R;
import com.example.memoryminder.SharedViewModel;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private LinearLayout buttonPersonalInfo, buttonChangePassword, buttonAbout;
    private Button buttonLogout;
    private TextView textViewMe;
    private SharedViewModel sharedViewModel;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        userRef = FirebaseDatabase.getInstance().getReference("Patients");

        // Initialize views
        textViewMe = root.findViewById(R.id.textViewMe);
        buttonPersonalInfo = root.findViewById(R.id.buttonPersonalInfo);
        buttonChangePassword = root.findViewById(R.id.buttonChangePassword);
        buttonAbout = root.findViewById(R.id.buttonAbout);
        buttonLogout = root.findViewById(R.id.buttonLogout);

        fetchAndSetUserName();

        // Set up button click listeners
        buttonPersonalInfo.setOnClickListener(view -> navigateToPersonalInfo());
        buttonChangePassword.setOnClickListener(view -> navigateToChangePassword());
        buttonAbout.setOnClickListener(view -> navigateToAbout());
        buttonLogout.setOnClickListener(view -> logout());


        return root;
    }

    private void fetchAndSetUserName() {
        String username = sharedViewModel.getUsername().getValue();
        if (username != null) {
            DatabaseReference userRefByUsername = userRef.child(username);
            userRefByUsername.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the dataSnapshot contains the user's details
                    if (dataSnapshot.exists()) {
                        Patient patient = dataSnapshot.getValue(Patient.class);
                        if (patient != null) {
                            String fullName = patient.getFirstName() + " " + patient.getLastName();
                            textViewMe.setText(fullName);
                        } else {
                            textViewMe.setText("User Name");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                    Log.e(TAG, "DatabaseError: " + databaseError.getMessage());
                    textViewMe.setText("Error loading name");
                }
            });
        } else {
            textViewMe.setText("Error: No username found");
        }
    }

    private void navigateToPersonalInfo() {
        String username = sharedViewModel.getUsername().getValue();
        Intent intent = new Intent(getActivity(), PersonalInfo.class);
        if (username != null) {
            intent.putExtra("USERNAME", username);
        }
        startActivity(intent);
    }

    private void navigateToChangePassword() {
        String username = sharedViewModel.getUsername().getValue();
        Intent intent = new Intent(getActivity(), ChangePassword.class);
        if (username != null) {
            intent.putExtra("USERNAME", username);
        }
        startActivity(intent);
    }

    private void navigateToAbout() {
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        startActivity(intent);
    }

    private void logout() {
        // Clear login state
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Show a confirmation dialog before logging out
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            // Navigate to LoginActivity
            Intent intent = new Intent(getActivity(), CombinedLoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Finish current activity
        });
        builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }
}
