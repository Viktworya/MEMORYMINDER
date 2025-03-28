package com.example.memoryminder;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavPat extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private long backPressedTime;
    private static final int TIME_INTERVAL = 2000; // Time interval in milliseconds for double back press

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navpat);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the bottom navigation view and the navigation controller
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_physical, R.id.navigation_cognitive,
                R.id.navigation_track, R.id.navigation_profile)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Retrieve the patient username from the Intent
        String username = getIntent().getStringExtra("PATIENT_USERNAME");

        // Initialize the SharedViewModel and set the username
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.setUsername(username);

        // Check for the TARGET_FRAGMENT extra and navigate accordingly
        String targetFragment = getIntent().getStringExtra("TARGET_FRAGMENT");
        if ("physical".equals(targetFragment)) {
            navController.navigate(R.id.navigation_physical);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Check if we are on the DashboardFragment
        if (navController.getCurrentDestination().getId() == R.id.navigation_dashboard) {
            // Handle double back press on the DashboardFragment
            if (backPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
                finish(); // Close the activity to exit the app
            } else {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
                backPressedTime = System.currentTimeMillis();
            }
        } else {
            // Navigate to DashboardFragment if not on it
            navController.navigate(R.id.navigation_dashboard);
            // Clear other fragments from the back stack
            navController.popBackStack(R.id.navigation_dashboard, false);
        }
    }
}
