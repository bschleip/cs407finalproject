package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Locale

class PostDetailActivity : AppCompatActivity() {

    private lateinit var usernameButton: MaterialButton
    private lateinit var mainImage: ImageView
    private lateinit var favoriteButton: ToggleButton
    private lateinit var likesCount: TextView
    private lateinit var geotag: TextView
    private lateinit var caption: TextView
    private lateinit var databaseHelper: UserDatabaseHelper
    private var postId: Int = -1
    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_post)

        initializeViews()
        postId = intent.getIntExtra("POST_ID", -1)
        if (postId != -1) {
            loadPost(postId)
            setupDeleteButton()
        } else finish()

        findViewById<Button>(R.id.backButton).apply {
            visibility = View.VISIBLE
            setOnClickListener { finish() }
        }
    }

    private fun setupDeleteButton() {
        findViewById<Button>(R.id.deleteButton).apply {
            visibility = if (getCurrentUserId() == post?.userId) View.VISIBLE else View.GONE
            setOnClickListener {
                AlertDialog.Builder(this@PostDetailActivity)
                    .setMessage("Delete this post?")
                    .setPositiveButton("Delete") { _, _ ->
                        databaseHelper.deletePost(postId)
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun initializeViews() {
        databaseHelper = UserDatabaseHelper(this)
        usernameButton = findViewById(R.id.username)
        mainImage = findViewById(R.id.mainImage)
        favoriteButton = findViewById(R.id.button_favorite)
        likesCount = findViewById(R.id.likes_count)
        geotag = findViewById(R.id.Geotag)
        caption = findViewById(R.id.Caption)

        usernameButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java)
                .putExtra("USER_ID", usernameButton.tag as Int))
        }

        favoriteButton.setOnClickListener {
            databaseHelper.toggleLike(postId, getCurrentUserId()!!)
            updateLikeUI()
        }
    }

    private fun loadPost(postId: Int) {
        val posts = databaseHelper.getAllPosts(false)
        post = posts.find { it.id == postId }
        post?.let { currentPost ->
            usernameButton.text = databaseHelper.getUsernameById(currentPost.userId)
            usernameButton.tag = currentPost.userId

            if (currentPost.imageUri.isNotEmpty()) {
                mainImage.setImageURI(Uri.parse(currentPost.imageUri))
            }

            caption.text = currentPost.caption
            if (currentPost.latitude != null && currentPost.longitude != null) {
                getAddressFromLocation(currentPost.latitude, currentPost.longitude) { address ->
                    geotag.text = "📍 $address"
                }
            } else {
                geotag.text = ""
            }

            updateLikeUI()
            findViewById<Button>(R.id.deleteButton).visibility =
                if (getCurrentUserId() == currentPost.userId) View.VISIBLE else View.GONE
        } ?: finish()
    }

    private fun updateLikeUI() {
        favoriteButton.isChecked = databaseHelper.hasUserLikedPost(postId, getCurrentUserId()!!)
        likesCount.text = databaseHelper.getLikeCount(postId).toString()
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val locality = address.locality ?: ""
                    val adminArea = address.adminArea ?: ""
                    runOnUiThread {
                        callback("$locality, $adminArea")
                    }
                } else {
                    runOnUiThread {
                        callback("$latitude, $longitude")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FeedActivity", "Error getting address", e)
            callback("$latitude, $longitude")
        }
    }
}