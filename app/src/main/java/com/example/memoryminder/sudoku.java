package com.example.memoryminder;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

/**
 * Main activity of the Sudoku game.
 */
public class sudoku extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku);
        configureButtons();
    }

    /**
     * Configures the click listeners for the buttons in the activity.
     */
    private void configureButtons() {
        Button easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> startClassicGameActivity(1));
    }

    /**
     * Starts the TwoPlayersActivity.
     */
    private void startTwoPlayersActivity() {
        startActivity(new Intent(this, TwoPlayersActivity.class));
    }

    /**
     * Starts the ClassicGameActivity with the specified game mode.
     *
     * @param mode The game mode: 1 for easy, 2 for medium, 3 for hard.
     */
    private void startClassicGameActivity(int mode) {
        Intent intent = new Intent(this, sudoku.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
}