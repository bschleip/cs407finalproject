package com.cs407.finalproject

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class SignupActivity : AppCompatActivity() {
    // Existing view binding remains the same
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var errorMessage: TextView

    private lateinit var dbHelper: UserDatabaseHelper //database helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize database helper
        dbHelper = UserDatabaseHelper(this)

        // Existing view bindings
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signupButton = findViewById(R.id.createAccountBtn)
        errorMessage = findViewById(R.id.errorMsgTextView)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Input validation
            when {
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showError("Empty parameters")
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    showError("Passwords don't match")
                    return@setOnClickListener
                }
                else -> {
                    registerUser(username, password) //attempt to register user
                }
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        // Check if username  exists
        if (dbHelper.usernameExists(username)) {
            showError("Username already exists")
            return
        }

        val result = dbHelper.registerUser(username, password)

        if (result != -1L) {
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        } else {
            showError("Failed to create account")
        }
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
    }

    // Optional: Override to close database when activity is destroyed
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    // Register a new user
    fun registerUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD_HASH, hashPassword(password))
        }

        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
        } catch (e: Exception) {
            -1 // Indicates registration failed (likely duplicate username)
        } finally {
            db.close()
        }
    }

    // Check if a username exists
    fun usernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        val cursor = db.query(
            TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return try {
            cursor.count > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Authenticate user (if needed for login functionality)
    fun authenticateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_PASSWORD_HASH)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        val cursor = db.query(
            TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return try {
            if (cursor.moveToFirst()) {
                val storedPasswordHash = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)
                )
                storedPasswordHash == hashPassword(password)
            } else {
                false
            }
        } finally {
            cursor.close()
            db.close()
        }
    }
}