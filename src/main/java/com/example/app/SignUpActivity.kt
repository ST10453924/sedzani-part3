package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivitySignUpBinding
import com.example.budget.DatabaseHelper

class SignUpActivity : AppCompatActivity() {

    // ViewBinding instance to access views defined in activity_sign_up.xml
    private lateinit var binding: ActivitySignUpBinding

    // Instance of custom DatabaseHelper to handle user registration in SQLite
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper to perform DB operations
        databaseHelper = DatabaseHelper(this)

        // Set a click listener on the Signup button
        binding.signupButton.setOnClickListener {
            // Retrieve the entered username and password from EditText fields
            val signupUsername = binding.signupUsername.text.toString()
            val signupPassword = binding.signupPassword.text.toString()

            // Call a function to handle signup and database insertion
            signupDatabase(signupUsername, signupPassword)
        }

        // Set a click listener on the "Login" text to redirect user to the LoginActivity
        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish this activity so it is removed from the back stack
        }
    }

    /**
     * This function handles inserting the new user's credentials into the database
     * and provides feedback via Toast.
     *
     * @param username The username entered by the user
     * @param password The password entered by the user
     */
    private fun signupDatabase(username: String, password: String) {
        // Insert the user into the database and get the result row ID
        val insertedRowId = databaseHelper.insertUser(username, password)

        // Check if insertion was successful
        if (insertedRowId != -1L) {
            // Show success message
            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close the SignUpActivity
        } else {
            // Show error message if signup failed (e.g., duplicate user)
            Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
