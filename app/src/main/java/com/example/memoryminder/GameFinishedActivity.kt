package com.example.memoryminder

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Activity displayed when the game is finished.
 */
class GameFinishedActivity : AppCompatActivity() {

    private var mode = 1
    private lateinit var activity: String
    private lateinit var database: DatabaseReference
    private var loggedInUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_finished)

        // Get the activity type from the intent extras
        activity = intent.getStringExtra("activity")!!

        // Get logged-in user's username from FirebaseAuth
        loggedInUsername = intent.getStringExtra("PATIENT_USERNAME")
        Log.d("MainActivity", "Received username: $loggedInUsername")

        // If username is null, use "Unknown"
        if (loggedInUsername.isNullOrEmpty()) {
            loggedInUsername = "Unknown"
        }

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Set up the text view with the appropriate content based on the activity type
        val textView = findViewById<TextView>(R.id.time)
        var text = ""
        when (activity) {
            "classic" -> {
                val mistakes = intent.getIntExtra("mistakesCount", 0)
                val minutes = intent.getIntExtra("minutes", 0)
                val seconds = intent.getIntExtra("seconds", 0)
                mode = intent.getIntExtra("mode", 1)
                text = "You finished the game in \n$minutes "
                text += if (minutes == 1) "minute"
                else "minutes"
                text += " and $seconds "
                text += if (seconds == 1) "second"
                else "seconds"
                text += ".\nYou made $mistakes "
                text += if (mistakes == 1) "mistake."
                else "mistakes."

                // Save the game results to Firebase under the user's node
                saveGameResults(loggedInUsername!!, minutes, mistakes, mode)
            }
        }
        textView.text = text

        // Log the received username
        Log.d("GameFinishedActivity", "Received username: $loggedInUsername")
    }

    private fun saveGameResults(username: String, minutes: Int, mistakes: Int, mode: Int) {
        val modeString = when (mode) {
            1 -> "easy"
            2 -> "medium"
            3 -> "hard"
            else -> "unknown"
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        // Save the game results under the user's node
        database.child("GameResults")
            .child(username) // Use the username as the node
            .child(modeString)
            .child(currentDate)
            .child("mistakes")
            .setValue("$mistakes")

        database.child("GameResults")
            .child(username)
            .child(modeString)
            .child(currentDate)
            .child("minutes")
            .setValue("$minutes")
    }

    override fun onBackPressed() {
        // Navigate back to the previous activity in the stack
        finish()
    }

    fun menu(view: View) {
        // Go back to the previous activity (MainActivity) in the stack
        finish()
    }

    fun again(view: View) {
        val intent = Intent(this, ClassicGameActivity::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("PATIENT_USERNAME", loggedInUsername)
        startActivity(intent)
        finish()
    }
}
