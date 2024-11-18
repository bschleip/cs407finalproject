package com.cs407.finalproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.security.MessageDigest


class SignupActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var errorMessage: TextView

    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswordKV: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signupButton = findViewById(R.id.createAccountBtn)
        errorMessage = findViewById(R.id.errorMsgTextView)

        userViewModel = UserViewModel()

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorMessage.text = "empty parameters"
                errorMessage.visibility = View.VISIBLE
            } else if (password != confirmPassword) {
                errorMessage.text = "passwords don't match"
                errorMessage.visibility = View.VISIBLE
            } else {
                // 1. make sure they're not in the database (using getUserPasswd)
                //      - if they are, error message
                // 2. otherwise, add them to database (in getUserPasswd) and log them in
                startActivity(Intent(this, CameraActivity::class.java))
            }
        }
    }

    private fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        val password = hash(passwdPlain)
        // check if username is in database, return false
        // else, add username and hashed password to database
        return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}