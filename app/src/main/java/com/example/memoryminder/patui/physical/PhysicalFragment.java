package com.example.memoryminder.patui.physical;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoryminder.R;
import com.example.memoryminder.SharedViewModel;
import com.example.memoryminder.Walklist;

public class PhysicalFragment extends Fragment {

    private Button btnSteps, btnJogging;
    private String loggedInUsername;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physical, container, false);

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe the username from the ViewModel
        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String username) {
                loggedInUsername = username;
            }
        });

        // Initialize buttons
        btnSteps = rootView.findViewById(R.id.btnSteps);
        //btnJogging = rootView.findViewById(R.id.btnJogging);

        // Set click listeners
        btnSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Walklist.class);
                intent.putExtra("PATIENT_USERNAME", loggedInUsername); // Pass the username
                startActivity(intent);
            }
        });

        //btnJogging.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {
                //Intent intent = new Intent(getActivity(), Joglist.class);
                //intent.putExtra("PATIENT_USERNAME", loggedInUsername); // Pass the username
                //startActivity(intent);
            //}
        //});

        return rootView;
    }
}
