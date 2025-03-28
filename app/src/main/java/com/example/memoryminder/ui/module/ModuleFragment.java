package com.example.memoryminder.ui.module;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.memoryminder.Modules;
import com.example.memoryminder.R;
import com.example.memoryminder.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModuleFragment extends Fragment {

    private LinearLayout patientListContainer;
    private DatabaseReference databaseReference;
    private Map<String, String> patientMap;
    private ArrayAdapter<String> adapter;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_module, container, false);

        patientListContainer = rootView.findViewById(R.id.patientListContainer);
        Button btnAddPatient = rootView.findViewById(R.id.btnAddPatient);

        // Initialize SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe the username LiveData
        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                // Initialize Firebase reference with the doctor's username
                databaseReference = FirebaseDatabase.getInstance().getReference("Doctors").child(username).child("Patients");
                // Load existing patients for this doctor
                loadPatients();
            }
        });

        btnAddPatient.setOnClickListener(v -> showSearchDialog());
        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);

        // Manually set the selected item to ModuleFragment
        navView.setSelectedItemId(R.id.navigation_module);

        return rootView;
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_patient, null);
        builder.setView(dialogView);

        EditText searchInput = dialogView.findViewById(R.id.etSearchPatient);
        ListView searchResults = dialogView.findViewById(R.id.lvSearchResults);

        patientMap = new HashMap<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>(patientMap.values()));
        searchResults.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = builder.create();
        searchResults.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPatientName = adapter.getItem(position);
            String selectedPatientUsername = getPatientUsername(selectedPatientName);
            addPatientToLayout(selectedPatientName, selectedPatientUsername);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void searchPatients(String query) {
        DatabaseReference patientsRef = FirebaseDatabase.getInstance().getReference("Patients");
        patientsRef.orderByChild("username").startAt(query).endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        patientMap.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String patientUsername = dataSnapshot.child("username").getValue(String.class);
                            String patientName = dataSnapshot.child("firstName").getValue(String.class) + " " +
                                    dataSnapshot.child("lastName").getValue(String.class);
                            patientMap.put(patientUsername, patientName);
                        }
                        adapter.clear();
                        adapter.addAll(patientMap.values());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void addPatientToLayout(String patientName, String patientUsername) {
        Button patientButton = new Button(requireContext());
        patientButton.setText(patientName);
        patientButton.setTextSize(18);
        patientButton.setBackgroundResource(R.drawable.cust);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16);
        patientButton.setLayoutParams(params);

        patientButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Modules.class);
            intent.putExtra("PATIENT_NAME", patientName);
            intent.putExtra("PATIENT_USERNAME", patientUsername);
            startActivity(intent);
        });

        patientButton.setOnLongClickListener(v -> {
            showRemoveConfirmationDialog(patientButton);
            return true;
        });

        patientListContainer.addView(patientButton);
        savePatientToFirebase(patientName, patientUsername);
    }

    private void showRemoveConfirmationDialog(Button patientButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Do you want to remove this patient?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    patientListContainer.removeView(patientButton);
                    removePatientFromFirebase(patientButton.getText().toString());
                    Toast.makeText(requireContext(), "Patient removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void savePatientToFirebase(String patientName, String patientUsername) {
        if (databaseReference != null) {
            databaseReference.child(patientUsername).setValue(patientName);
        }
    }

    private void removePatientFromFirebase(String patientName) {
        if (databaseReference != null) {
            databaseReference.orderByValue().equalTo(patientName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                        patientSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void loadPatients() {
        if (databaseReference != null) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                        String patientName = patientSnapshot.getValue(String.class);
                        String patientUsername = patientSnapshot.getKey();
                        addPatientToLayout(patientName, patientUsername);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private String getPatientUsername(String patientName) {
        for (Map.Entry<String, String> entry : patientMap.entrySet()) {
            if (entry.getValue().equals(patientName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
