package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class SignupActivity : AppCompatActivity() {
    // View bindings
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    private lateinit var errorMessage: TextView

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        dbHelper = UserDatabaseHelper(this)

        // Bind views
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signupButton = findViewById(R.id.createAccountBtn)
        loginButton = findViewById(R.id.loginButton)
        errorMessage = findViewById(R.id.errorMsgTextView)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Input validation
            when {
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showError("All fields are required")
                }
                password != confirmPassword -> {
                    showError("Passwords do not match")
                }
                else -> {
                    registerUser(username, password)
                }
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(username: String, password: String) {
        // Check if username already exists
        if (dbHelper.usernameExists(username)) {
            showError("Username already exists")
            return
        }

        // Register user and get result
        val result = dbHelper.registerUser(username, password)
        if (result != -1L) {
            saveUserSession(result.toInt())
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
            // Redirect to CameraActivity
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            showError("Failed to create account")
        }
    }

    private fun saveUserSession(userId: Int) {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("LOGGED_IN_USER_ID", userId)
            apply()
        }
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        dbHelper.close() // Close the database when the activity is destroyed
        super.onDestroy()
    }
}


