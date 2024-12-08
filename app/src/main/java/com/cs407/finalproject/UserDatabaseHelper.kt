package com.cs407.finalproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getDoubleOrNull
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5
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
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"

        // Like table columns
        private const val TABLE_LIKES = "user_likes"
        private const val COLUMN_LIKE_POST_ID = "post_id"
        private const val COLUMN_LIKE_USER_ID = "user_id"

        // Friends table columns
        private const val TABLE_FRIENDS = "friends"
        private const val COLUMN_FRIEND_USER_ID = "user_id"
        private const val COLUMN_FRIEND_ID = "friend_id"

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
                $COLUMN_TIMESTAMP DATETIME DEFAULT (datetime('now', 'localtime')),
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        val createLikesTable = """
            CREATE TABLE $TABLE_LIKES (
                $COLUMN_LIKE_POST_ID INTEGER,
                $COLUMN_LIKE_USER_ID INTEGER,
                PRIMARY KEY ($COLUMN_LIKE_POST_ID, $COLUMN_LIKE_USER_ID),
                FOREIGN KEY($COLUMN_LIKE_POST_ID) REFERENCES $TABLE_POSTS($COLUMN_POST_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_LIKE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        val createFriendsTable = """
            CREATE TABLE $TABLE_FRIENDS (
                $COLUMN_FRIEND_USER_ID INTEGER NOT NULL,
                $COLUMN_FRIEND_ID INTEGER NOT NULL,
                PRIMARY KEY ($COLUMN_FRIEND_USER_ID, $COLUMN_FRIEND_ID),
                FOREIGN KEY($COLUMN_FRIEND_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_FRIEND_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createFriendsTable)


        db.execSQL(createUsersTable)
        db.execSQL(createPostsTable)
        db.execSQL(createLikesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LIKES ")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FRIENDS")
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

    fun getUsernameById(userId: Int): String {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "$COLUMN_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            } else {
                "Unknown User"
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Post-related methods
    fun addPost(userId: Int, imageUri: String, caption: String?, latitude: Double?, longitude: Double?): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_CAPTION, caption)
            put(COLUMN_LIKES, 0) // Initial likes count
            put(COLUMN_TIMESTAMP, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date()))
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
        }
        return try {
            db.insertOrThrow(TABLE_POSTS, null, values)
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Error adding post: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    fun getShenaniganOfTheDay(): Post? {
        val db = this.readableDatabase
        // First try to get today's most liked post
        var cursor = db.rawQuery(
            """
        SELECT * FROM $TABLE_POSTS
        WHERE date($COLUMN_TIMESTAMP) = date('now', 'localtime')
        ORDER BY $COLUMN_LIKES DESC, $COLUMN_TIMESTAMP DESC
        LIMIT 1
        """, null
        )

        // If no posts today, get the most recent previous Shenanigan of the Day
        if (!cursor.moveToFirst()) {
            cursor.close()
            cursor = db.rawQuery(
                """
            SELECT * FROM $TABLE_POSTS
            WHERE date($COLUMN_TIMESTAMP) < date('now', 'localtime')
            ORDER BY date($COLUMN_TIMESTAMP) DESC, $COLUMN_LIKES DESC
            LIMIT 1
            """, null
            )
        }

        return if (cursor.moveToFirst()) {
            Post(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POST_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                caption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTION)),
                likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                latitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE))) null
                else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                longitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))) null
                else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
            )
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun getAllPosts(excludeShenanigan: Boolean = true): List<Post> {
        val posts = mutableListOf<Post>()
        val db = this.readableDatabase

        // Get current Shenanigan of the Day ID to exclude
        val shenanigan = if (excludeShenanigan) getShenaniganOfTheDay() else null
        val shenaniganId = shenanigan?.id

        // Build query with optional exclusion
        val selection = if (shenaniganId != null) "$COLUMN_POST_ID != ?" else null
        val selectionArgs = if (shenaniganId != null) arrayOf(shenaniganId.toString()) else null

        val cursor = db.query(
            TABLE_POSTS,
            null,
            selection,
            selectionArgs,
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
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        latitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE))) null
                        else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        longitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))) null
                        else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                    )
                    posts.add(post)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
            db.close() // Only close the database here, after all operations are complete
        }
        return posts
    }

    // Like counting-related methods
    fun hasUserLikedPost(postId: Int, userId: Int): Boolean {
        val db = this.readableDatabase
        return db.query(
            TABLE_LIKES,
            null,
            "$COLUMN_LIKE_POST_ID = ? AND $COLUMN_LIKE_USER_ID = ?",
            arrayOf(postId.toString(), userId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    fun toggleLike(postId: Int, userId: Int) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            if(hasUserLikedPost(postId, userId)) {
                // Unlike the post
                db.delete(
                    TABLE_LIKES,
                    "$COLUMN_LIKE_POST_ID = ? AND $COLUMN_LIKE_USER_ID = ?",
                    arrayOf(postId.toString(), userId.toString())
                )
                db.execSQL(
                    "UPDATE $TABLE_POSTS SET $COLUMN_LIKES = $COLUMN_LIKES - 1 WHERE $COLUMN_POST_ID = ?",
                    arrayOf(postId)
                )
            } else {
                // Like the post
                val values = ContentValues().apply {
                    put(COLUMN_LIKE_POST_ID, postId)
                    put(COLUMN_LIKE_USER_ID, userId)
                }
                db.insertOrThrow(TABLE_LIKES, null, values)
                db.execSQL(
                    "UPDATE $TABLE_POSTS SET $COLUMN_LIKES = $COLUMN_LIKES + 1 WHERE $COLUMN_POST_ID = ?",
                    arrayOf(postId)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // TODO: we should probably show the like counting
    fun getLikeCount(postId: Int): Int {
        val db = this.readableDatabase
        return db.query(
            TABLE_LIKES,
            arrayOf("COUNT(*)"),
            "$COLUMN_LIKE_POST_ID = ?",
            arrayOf(postId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    // Friend-related methods
    fun addFriend(userId: Int, friendUsername: String): Boolean {
        val db = this.writableDatabase
        val friendId = getUserIdByUsername(friendUsername)
        return if (friendId != null && friendId != userId) {
            db.beginTransaction()
            try {
                // Add friendship in both directions
                val values1 = ContentValues().apply {
                    put(COLUMN_FRIEND_USER_ID, userId)
                    put(COLUMN_FRIEND_ID, friendId)
                }
                val values2 = ContentValues().apply {
                    put(COLUMN_FRIEND_USER_ID, friendId)
                    put(COLUMN_FRIEND_ID, userId)
                }
                val success = db.insert(TABLE_FRIENDS, null, values1) != -1L &&
                        db.insert(TABLE_FRIENDS, null, values2) != -1L
                db.setTransactionSuccessful()
                success
            } finally {
                db.endTransaction()
            }
        } else {
            false
        }
    }

    private fun getUserIdByUsername(username: String): Int? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS, arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?", arrayOf(username),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else null
        }
    }

    // TODO: this function is unused; we can get rid of it if it's unnecessary
    fun getFriendList(userId: Int): List<Int> {
        val friendList = mutableListOf<Int>()
        val db = readableDatabase
        val cursor = db.query(
            "friends",
            arrayOf("friendId"),
            "userId=?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            friendList.add(cursor.getInt(cursor.getColumnIndexOrThrow("friendId")))
        }
        cursor.close()
        return friendList
    }

    // TODO: this function is unused; we can get rid of it if it's unnecessary
    fun getPostsByUserIds(userIds: List<Int>): List<Post> {
        if (userIds.isEmpty()) return emptyList()

        val db = this.readableDatabase
        val posts = mutableListOf<Post>()
        val userIdString = userIds.joinToString(",") // Convert list to a comma-separated string

        val query = """
        SELECT * FROM $TABLE_POSTS
        WHERE $COLUMN_USER_ID IN ($userIdString)
        ORDER BY $COLUMN_TIMESTAMP DESC
    """

        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val post = Post(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POST_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                    caption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTION)),
                    likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    latitude = cursor.getDoubleOrNull(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    longitude = cursor.getDoubleOrNull(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                )
                posts.add(post)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return posts
    }

    fun getFriends(userId: Int): List<Int> {
        val db = this.readableDatabase
        val friendIds = mutableListOf<Int>()
        val query = """
        SELECT u.$COLUMN_ID FROM $TABLE_USERS u
        INNER JOIN $TABLE_FRIENDS f ON u.$COLUMN_ID = f.$COLUMN_FRIEND_ID
        WHERE f.$COLUMN_FRIEND_USER_ID = ?
    """
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                friendIds.add(it.getInt(0)) // Get user ID instead of username
            }
        }
        return friendIds
    }

    fun removeFriend(userId: Int, friendUsername: String): Boolean {
        val db = this.writableDatabase
        val friendId = getUserIdByUsername(friendUsername)
        return if (friendId != null) {
            db.beginTransaction()
            try {
                // Remove friendship in both directions
                val count1 = db.delete(
                    TABLE_FRIENDS,
                    "$COLUMN_FRIEND_USER_ID = ? AND $COLUMN_FRIEND_ID = ?",
                    arrayOf(userId.toString(), friendId.toString())
                )
                val count2 = db.delete(
                    TABLE_FRIENDS,
                    "$COLUMN_FRIEND_USER_ID = ? AND $COLUMN_FRIEND_ID = ?",
                    arrayOf(friendId.toString(), userId.toString())
                )
                val success = count1 > 0 && count2 > 0
                db.setTransactionSuccessful()
                success
            } finally {
                db.endTransaction()
            }
        } else {
            false
        }
    }
}