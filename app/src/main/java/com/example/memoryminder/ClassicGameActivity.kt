package com.example.memoryminder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Activity for playing the classic Sudoku game.
 */
class ClassicGameActivity : AppCompatActivity() {

    private lateinit var viewModel: SudokuViewModel
    private var mode = 1
    private lateinit var loggedInUsername: String
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classic_game)

        // Get the logged-in username from the Intent
        loggedInUsername = intent.getStringExtra("PATIENT_USERNAME") ?: ""
        Log.d("ClassicGameActivity", "Received username: $loggedInUsername")

        // Get the game mode from the intent extras
        mode = intent.getIntExtra("mode", 1)

        // Set up Sudoku board view
        val boardView = findViewById<SudokuBoardView>(R.id.sudokuBoardView)
        boardView.onTouchListener = object : SudokuBoardView.OnTouchListener {
            override fun onTouch(row: Int, column: Int) {
                onCellTouched(row, column)
            }
        }

        // Initialize ViewModel
        val viewModelFactory = ViewModelFactory(mode)
        viewModel = ViewModelProvider(this, viewModelFactory)[SudokuViewModel::class.java]
        viewModel.selectedCell.observe(this) { updateSelectedCell(it) }
        viewModel.boardNumbers.observe(this) { updateBoardNumbers(it) }
        viewModel.seconds.observe(this) { updateTime(it) }

        // Set up number buttons
        val buttonList = listOf(
            findViewById<Button>(R.id.oneButton),
            findViewById<Button>(R.id.twoButton),
            findViewById<Button>(R.id.threeButton),
            findViewById<Button>(R.id.fourButton),
            findViewById<Button>(R.id.fiveButton),
            findViewById<Button>(R.id.sixButton),
            findViewById<Button>(R.id.sevenButton),
            findViewById<Button>(R.id.eightButton),
            findViewById<Button>(R.id.nineButton)
        )
        buttonList.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.numberInput(index + 1) }
        }

        // Set up accept button
        val acceptButton = findViewById<Button>(R.id.acceptButton)
        acceptButton.setOnClickListener { viewModel.acceptNumber() }

        // Set up remove button
        val removeButton = findViewById<Button>(R.id.backButton)
        removeButton.setOnClickListener { viewModel.removeNumber() }

        // Set up finish button
        val finishButton = findViewById<Button>(R.id.finishButton)
        finishButton.setOnClickListener {
            if (viewModel.finish()) end()
        }

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference
    }

    /**
     * Ends the game and starts a new activity to display the game results.
     */
    private fun end() {
        val handler = Handler()

        val startNewActivityRunnable = Runnable {
            val intent = Intent(this, GameFinishedActivity::class.java).apply {
                putExtra("activity", "classic")
                putExtra("mistakesCount", viewModel.mistakes)
                putExtra("minutes", viewModel.minutes.value)
                putExtra("seconds", viewModel.seconds.value)
                putExtra("mode", mode)
                putExtra("PATIENT_USERNAME", loggedInUsername) // Pass the username to GameFinishedActivity
            }
            startActivity(intent)
            finish()
        }
        handler.postDelayed(startNewActivityRunnable, 2000)
    }

    /**
     * Updates the selected cell on the Sudoku board view.
     */
    private fun updateSelectedCell(cell: Pair<Int, Int>?) = cell?.let {
        val boardView = findViewById<SudokuBoardView>(R.id.sudokuBoardView)
        boardView.updateSelectedCell(cell.first, cell.second)
    }

    /**
     * Updates the numbers displayed on the Sudoku board view.
     */
    private fun updateBoardNumbers(boardNumbers: List<Pair<Int, Int>?>) {
        val boardView = findViewById<SudokuBoardView>(R.id.sudokuBoardView)
        boardView.updateBoardNumbers(boardNumbers)
    }

    /**
     * Updates the time display on the activity.
     */
    private fun updateTime(seconds: Int) {
        var text = viewModel.minutes.value.toString() + ":"
        if (viewModel.minutes.value!! < 10) {
            text = "0$text"
        }
        if (seconds < 10) {
            text += "0$seconds"
        } else {
            text += seconds
        }
        val timeTextView = findViewById<TextView>(R.id.time)
        timeTextView.text = text
    }

    /**
     * Callback when a cell on the Sudoku board is touched.
     */
    private fun onCellTouched(row: Int, column: Int) {
        viewModel.updateSelectedCell(row, column)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Do you really want to quit?")
            .setPositiveButton("Yes") { _, _ ->
                // Navigate back to the previous activity in the stack
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
