package com.example.memoryminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {
    private var loggedInUsername: String? = null
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sudoku)

        // Get the logged-in username from the intent
        loggedInUsername = intent.getStringExtra("PATIENT_USERNAME")
        Log.d("MainActivity", "Received username: $loggedInUsername")

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference

        // Configure buttons
        configureButtons()
    }

    override fun onBackPressed() {
        // Finish the activity when the back button is pressed
        finish()
    }

    private fun configureButtons() {
        findViewById<Button>(R.id.easyButton).setOnClickListener {
            // Check patient's stage before starting the game
            checkPatientStageAndStartGame(1)
        }
    }

    private fun checkPatientStageAndStartGame(mode: Int) {
        loggedInUsername?.let { username ->
            val patientStageRef = databaseReference.child("Patients").child(username).child("stage")

            patientStageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val patientStage = dataSnapshot.getValue(String::class.java)
                    Log.d("MainActivity", "Patient stage: $patientStage")

                    if ("Mild".equals(patientStage, ignoreCase = true)) {
                        startClassicGameActivity(mode)
                    } else {
                        showAlert("You are not recommended to play this game")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("MainActivity", "Failed to read patient stage: ${databaseError.message}")
                }
            })
        }
    }


    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Game Access")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun startClassicGameActivity(mode: Int) {
        val intent = Intent(this, ClassicGameActivity::class.java).apply {
            putExtra("mode", mode)
            putExtra("PATIENT_USERNAME", loggedInUsername) // Pass the username
        }
        startActivity(intent)
    }
}
