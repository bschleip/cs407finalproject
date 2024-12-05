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

        // Users table columns
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD_HASH = "password_hash"

        // Posts table columns
        private const val TABLE_POSTS = "posts"
        private const val COLUMN_POST_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_IMAGE_URI = "image_uri"
        private const val COLUMN_CAPTION = "caption"
        private const val COLUMN_LIKES = "likes"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_LOCATION = "location"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL
            )
        """.trimIndent()

        val createPostsTable = """
            CREATE TABLE $TABLE_POSTS (
                $COLUMN_POST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_IMAGE_URI TEXT NOT NULL,
                $COLUMN_CAPTION TEXT,
                $COLUMN_LIKES INTEGER DEFAULT 0,
                $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent() // TODO: location column

        db.execSQL(createUsersTable)
        db.execSQL(createPostsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        onCreate(db)
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // User-related methods
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

    // Post-related methods
    fun addPost(userId: Int, imageUri: String, caption: String?): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_CAPTION, caption)
            put(COLUMN_LIKES, 0) // Initial likes count
        }
        return db.insert(TABLE_POSTS, null, values).also {
            db.close()
        }
    }

    fun getAllPosts(): List<Post> {
        val posts = mutableListOf<Post>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_POSTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
        try {
            if (cursor.moveToFirst()) {
                do {
                    val post = Post(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POST_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                        caption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTION)),
                        likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                    posts.add(post)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
            db.close()
        }
    return posts
    }

    fun getFeaturedPost(): Post? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT * FROM $TABLE_POSTS
                WHERE DATE($COLUMN_TIMESTAMP) = DATE('now')
                ORDER BY $COLUMN_LIKES DESC
                LIMIT 1
            """, null
        )
        return if (cursor.moveToFirst()) {
            Post(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POST_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                caption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTION)),
                likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun likePost(postId: Int) {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_POSTS SET $COLUMN_LIKES = $COLUMN_LIKES + 1 WHERE $COLUMN_POST_ID = ?", arrayOf(postId))
    }

    fun updateLikes(postId: Int, likes: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("likes", likes)
        }
        db.update("posts", values, "id = ?", arrayOf(postId.toString()))
        db.close()
    }

    // Potential functions to avoid null crashing at first
    fun tableExists(db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='posts'", null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun addDefaultPost(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put("caption", "Welcome to the app!")
        values.put("imageUri", "")  // You can use a default image or leave it empty
        values.put("timestamp", System.currentTimeMillis())
        db.insert("posts", null, values)
    }
}