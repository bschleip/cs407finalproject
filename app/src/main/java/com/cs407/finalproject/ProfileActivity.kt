package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class ProfileActivity : AppCompatActivity() {
    private lateinit var feedButton: Button
    private lateinit var cameraButton: Button
    private lateinit var profileButton: Button
    private lateinit var username: TextView
    private lateinit var bio: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var profileImage: ImageView

    private lateinit var databaseHelper: UserDatabaseHelper
    private var posts = listOf<Post>()
    private var isCurrentUserProfile = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userId: Int = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: requireNotNull(getCurrentUserId())
        isCurrentUserProfile = (userId == getCurrentUserId())

        initializeViews()
        setupNavigation()

        setupProfile(userId)
        setupBioEditing()
        setupProfileImageHandling()
        setupPostsGrid()
    }

    private fun initializeViews() {
        feedButton = findViewById(R.id.nav_home)
        cameraButton = findViewById(R.id.nav_cam)
        profileButton = findViewById(R.id.nav_profile)
        databaseHelper = UserDatabaseHelper(this)
        username = findViewById(R.id.profileName)
        bio = findViewById(R.id.profileBio)
        recyclerView = findViewById(R.id.recyclerView)
        profileImage = findViewById(R.id.profileImage)
    }

    private fun setupNavigation() {
        feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupProfile(userId: Int) {
        posts = databaseHelper.getPostsByUserIds(listOf(userId))
        username.text = databaseHelper.getUsernameById(userId)
        val userBio = databaseHelper.getUserBio(userId)
        bio.text = if (userBio.isBlank()) "No bio yet!" else userBio
    }

    private fun setupBioEditing() {
        if (!isCurrentUserProfile) return

        bio.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this)
            input.setText(bio.text)

            builder.setView(input)
                .setPositiveButton("Update") { _, _ ->
                    val newBio = input.text.toString()
                    bio.text = newBio
                    databaseHelper.updateUserBio(getCurrentUserId()!!, newBio)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupProfileImageHandling() {
        if (isCurrentUserProfile) {
            profileImage.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }
        }
        val userId: Int = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: requireNotNull(getCurrentUserId())
        loadProfileImage(userId)
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            // Copy the image to app's private storage
            val fileName = "profile_${getCurrentUserId()}_${System.currentTimeMillis()}.jpg"
            val fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            val inputStream = contentResolver.openInputStream(uri)

            inputStream?.use { input ->
                fileOutputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Create a URI for the saved file
            val internalUri = Uri.fromFile(File(filesDir, fileName)).toString()

            // Save to database
            getCurrentUserId()?.let { userId ->
                databaseHelper.updateUserProfileImage(userId, internalUri)
            }

            // Update UI
            profileImage.setImageURI(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(profileUserId: Int) {
        val imageUri = databaseHelper.getUserProfileImageUri(profileUserId)

        if (!imageUri.isNullOrEmpty()) {
            try {
                profileImage.setImageURI(Uri.parse(imageUri))
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.logo)
            }
        } else {
            profileImage.setImageResource(R.drawable.logo)
        }
    }

    private fun setupPostsGrid() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = object : RecyclerView.Adapter<PostViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.post_profile, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
                val post = posts[position]
                holder.bindPost(post)

                holder.itemView.setOnClickListener {
                    startActivity(Intent(this@ProfileActivity, PostDetailActivity::class.java)
                        .putExtra("POST_ID", post.id))
                }

                if (isCurrentUserProfile) {
                    holder.itemView.setOnLongClickListener {
                        showDeleteDialog(post)
                        true
                    }
                }
            }

            override fun getItemCount(): Int = posts.size
        }
    }

    private fun showDeleteDialog(post: Post) {
        AlertDialog.Builder(this)
            .setMessage("Delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                databaseHelper.deletePost(post.id)
                posts = databaseHelper.getPostsByUserIds(listOf(getCurrentUserId()!!))
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    override fun onResume() {
        super.onResume()
        val userId: Int = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: requireNotNull(getCurrentUserId())
        loadProfileImage(userId)
    }

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainImage: ImageView = view.findViewById(R.id.mainImage)

        fun bindPost(post: Post) {
            if (post.imageUri.isNotEmpty()) {
                mainImage.setImageURI(Uri.parse(post.imageUri))
            } else {
                mainImage.setImageResource(R.drawable.expost2)
            }
        }
    }
}