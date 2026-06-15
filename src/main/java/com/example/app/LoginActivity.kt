package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.app.databinding.ActivityLoginBinding
import com.example.budget.DatabaseHelper

// Activity that handles user login logic
class LoginActivity : AppCompatActivity() {

    // ViewBinding object to access views from the layout
    private lateinit var binding: ActivityLoginBinding

    // Instance of DatabaseHelper to interact with SQLite database
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root) // Sets the root view for the activity

        // Initialize the DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Handle login button click
        binding.loginButton.setOnClickListener {
            // Get text from username and password input fields
            val loginUsername = binding.loginUsername.text.toString()
            val loginPassword = binding.loginPassword.text.toString()

            // Call function to check credentials
            loginDatabase(loginUsername, loginPassword)
        }

        // Handle redirect to sign-up screen
        binding.signupRedirect.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish() // Closes LoginActivity so the user can’t return using the back button
        }
    }

    // Function to check if the user exists in the database
    private fun loginDatabase(username: String, password: String) {
        val userExists = databaseHelper.readUser(username, password)

        if (userExists) {
            // Show success message and redirect to main screen
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Prevents returning to login screen
        } else {
            // Show failure message
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
