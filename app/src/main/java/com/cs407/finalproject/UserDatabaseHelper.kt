package com.cs407.finalproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

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
            .joinToString("") { "%02x".format(it) }
    }

    fun registerUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD_HASH, hashPassword(password))
        }
        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
        } catch (e: Exception) {
            -1 // Registration failed (e.g., duplicate username)
        } finally {
            db.close()
        }
    }

    fun usernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        return try {
            cursor.count > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_PASSWORD_HASH),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        return try {
            if (cursor.moveToFirst()) {
                val storedPasswordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
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