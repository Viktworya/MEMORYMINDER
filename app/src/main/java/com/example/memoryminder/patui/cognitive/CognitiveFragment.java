package com.example.memoryminder.patui.cognitive;

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

import com.example.memoryminder.MainActivity;
import com.example.memoryminder.MemoryGameModerate;
import com.example.memoryminder.Menu;
import com.example.memoryminder.R;
import com.example.memoryminder.SharedViewModel;

public class CognitiveFragment extends Fragment {

    private Button btnSudoku;
    private Button btnMemory;
    private String loggedInUsername;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cognitive, container, false);

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
        btnSudoku = rootView.findViewById(R.id.btnSudoku);
        btnMemory = rootView.findViewById(R.id.btnMemory);

        // Set click listeners
        btnSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Sudoku activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("PATIENT_USERNAME", loggedInUsername); // Pass the username
                startActivity(intent);
            }
        });

        btnMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Sudoku activity
                Intent intent = new Intent(getActivity(), Menu.class);
                intent.putExtra("PATIENT_USERNAME", loggedInUsername); // Pass the username
                startActivity(intent);
            }
        });

        return rootView;
    }
}
