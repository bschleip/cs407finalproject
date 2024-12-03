package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.autofill.UserData
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    private lateinit var errorMessage: TextView

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        dbHelper = UserDatabaseHelper(this)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signupButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)
        errorMessage = findViewById(R.id.errorMsgTextView)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Input validation
            when {
                username.isEmpty() || password.isEmpty() -> {
                    showError("All fields are required")
                }
                else -> {
                    // inputs are valid; checking the database
                    val isAuthenticated = dbHelper.authenticateUser(username, password)
                    if (isAuthenticated) {
                        val userId = getUserIdByUsername(username)
                        if (userId != null) {
                            saveUserSession(userId)
                            startActivity(Intent(this, CameraActivity::class.java))
                        } else {
                            showError("Username not found")
                        }
                    } else {
                        showError("Incorrect username or password")
                    }
                }
            }
        }

        // redirect to the signup page
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun getUserIdByUsername(username: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "users",
            arrayOf("id"),
            "username = ?",
            arrayOf(username),
            null, null, null
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            } else{
                null
            }
        } finally {
            cursor.close()
            db.close()
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
        dbHelper.close() // Close database when activity is destroyed
        super.onDestroy()
    }
}
