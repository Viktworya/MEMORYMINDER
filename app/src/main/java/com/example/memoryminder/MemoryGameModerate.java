package com.example.memoryminder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameModerate extends AppCompatActivity {

    ImageView image1, image2, image3, image4,
            image5, image6, image7, image8,
            image9, image10;

    List<ImageView> tilesList;
    List<Integer> imagesList;

    int clickNumber = 1;
    int click1Value = 0;
    int click2Value = 0;
    TextView timerTextView;

    Handler timerHandler;
    Runnable timerRunnable;

    int seconds = 0;
    boolean gameFinished = false;
    private String patientUsername; // Variable to hold the username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memorygame);

        // Receive the username from the intent
        Intent intent = getIntent();
        patientUsername = intent.getStringExtra("PATIENT_USERNAME");

        // Initialize ImageViews
        initializeImageViews();

        // Initialize the timer TextView
        timerTextView = findViewById(R.id.timerTextView);

        // Initialize tile and image lists
        initializeTileAndImageLists();

        // Show all tiles initially
        showAllTilesInitially();

        // Start the timer after a delay (so it begins after the tiles flip back)
        new Handler().postDelayed(this::startTimer, 3000);
    }

    private void initializeImageViews() {
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);
        image10 = findViewById(R.id.image10);

        // Set onClick listeners
        for (ImageView imageView : getAllImageViews()) {
            imageView.setOnClickListener(v -> buttonClick(imageView, getImageIndex(imageView)));
        }
    }

    private void initializeTileAndImageLists() {
        tilesList = new ArrayList<>(getAllImageViews());

        imagesList = new ArrayList<>();
        Collections.addAll(imagesList, R.drawable.image1, R.drawable.image2, R.drawable.image3,
                R.drawable.image4, R.drawable.image5,
                R.drawable.image1, R.drawable.image2, R.drawable.image3,
                R.drawable.image4, R.drawable.image5);

        // Shuffle images
        Collections.shuffle(imagesList);
    }

    private List<ImageView> getAllImageViews() {
        return List.of(image1, image2, image3, image4, image5, image6,
                image7, image8, image9, image10);
    }

    private int getImageIndex(ImageView imageView) {
        return getAllImageViews().indexOf(imageView);
    }

    private void showAllTilesInitially() {
        // Show all images initially
        for (int i = 0; i < tilesList.size(); i++) {
            tilesList.get(i).setImageResource(imagesList.get(i));
        }

        // After 5 seconds, flip the tiles back to the default state
        new Handler().postDelayed(this::hideAllTiles, 5000);
    }

    private void hideAllTiles() {
        // Flip all tiles back to the hidden state (default tile image)
        for (ImageView imageView : tilesList) {
            imageView.setImageResource(R.drawable.tile);
        }

        // Enable all tiles to be clickable now
        enableAllTiles();
    }

    private void startTimer() {
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameFinished) {
                    seconds++;
                    timerTextView.setText(formatTime(seconds));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("Time: %02d:%02d", minutes, seconds);
    }

    private void buttonClick(ImageView image, int number) {
        if (clickNumber == 1) {
            image.setImageResource(imagesList.get(number));
            clickNumber = 2;
            click1Value = number + 1;
        } else if (clickNumber == 2) {
            image.setImageResource(imagesList.get(number));
            clickNumber = 1;
            click2Value = number + 1;
            compareTiles();
        }
    }

    private void compareTiles() {
        // Disable all tiles
        for (ImageView imageView : tilesList) {
            imageView.setEnabled(false);
        }

        if (imagesList.get(click1Value - 1).equals(imagesList.get(click2Value - 1))) {
            // Same images
            new Handler().postDelayed(() -> {
                tilesList.get(click1Value - 1).setVisibility(View.INVISIBLE);
                tilesList.get(click2Value - 1).setVisibility(View.INVISIBLE);
                if (allTilesMatched()) {
                    endGame();
                } else {
                    enableAllTiles();
                }
            }, 1000);
        } else {
            // Different images
            new Handler().postDelayed(() -> {
                tilesList.get(click1Value - 1).setImageResource(R.drawable.tile);
                tilesList.get(click2Value - 1).setImageResource(R.drawable.tile);
                enableAllTiles();
            }, 1000);
        }
    }

    private boolean allTilesMatched() {
        for (ImageView imageView : tilesList) {
            if (imageView.getVisibility() != View.INVISIBLE) {
                return false;
            }
        }
        return true;
    }

    private void endGame() {
        gameFinished = true;
        timerHandler.removeCallbacks(timerRunnable);

        // Navigate to GameOverActivity and pass the username
        Intent intent = new Intent(MemoryGameModerate.this, GameOverActivitySevere.class);
        intent.putExtra("PATIENT_USERNAME", patientUsername);
        intent.putExtra("FINAL_SCORE", seconds);
        startActivity(intent);
        finish();
    }

    private void enableAllTiles() {
        for (ImageView imageView : tilesList) {
            if (imageView.getVisibility() != View.INVISIBLE) {
                imageView.setEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (gameFinished) {
            super.onBackPressed();
        } else {
            showQuitDialog();
        }
    }

    private void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to quit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Quit the game and go back to the previous activity
                    finish();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
