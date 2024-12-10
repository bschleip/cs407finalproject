package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        feedButton = findViewById(R.id.nav_home)
        cameraButton = findViewById(R.id.nav_cam)
        profileButton = findViewById(R.id.nav_profile)
        databaseHelper = UserDatabaseHelper(this)
        username = findViewById(R.id.profileName)
        bio = findViewById(R.id.profileBio)
        recyclerView = findViewById(R.id.recyclerView)
        profileImage = findViewById(R.id.profileImage)

        feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        profileImage.setOnClickListener {
            //had to figure out how to get access to system images
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
                imagePickerLauncher.launch(intent)
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 1000)
            }
        }
        bio.setOnClickListener {
            val editText = EditText(this)
            val dialog = AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    val userInput = editText.text.toString()
                    if (userInput.isNotEmpty()) {
                        bio.text = userInput
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        // username and posts stuff
        val userId: Int = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: requireNotNull(getCurrentUserId())
        isCurrentUserProfile = (userId == getCurrentUserId())

        setupProfile(userId)
        setupBioEditing()
        setupPostsGrid(userId)
        loadProfileImage()
    }


    private fun setupProfile(userId: Int) {
        posts = databaseHelper.getPostsByUserIds(listOf(userId))
        username.text = databaseHelper.getUsernameById(userId)
        bio.text = databaseHelper.getUserBio(userId)
    }

    private fun setupBioEditing() {
        if (!isCurrentUserProfile) return

        bio.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this)
            input.setText(bio.text)

            builder.setView(input)
                .setPositiveButton("Update") { _,_ ->
                    val newBio = input.text.toString()
                    bio.text = newBio
                    databaseHelper.updateUserBio(getCurrentUserId()!!, newBio)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupPostsGrid(userId: Int) {
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

            override fun getItemCount(): Int { return posts.size }
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

    //profile image stuff
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            if (r.resultCode == RESULT_OK) {
                val imageUri: Uri? = r.data?.data
                if (imageUri != null) {
                    profileImage.setImageURI(imageUri)
                    saveProfileImageUri(imageUri.toString())
                }
            }
        }
    private fun saveProfileImageUri(uri: String) {
        val pref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        with(pref.edit()) {
            putString("PROFILE_IMAGE_URI", uri)
            apply()
        }
    }
    private fun loadProfileImage() {
        val pref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val uri = pref.getString("PROFILE_IMAGE_URI", null)
        if (!uri.isNullOrEmpty()) {
            try {
                val uriNew = Uri.parse(uri)
                profileImage.setImageURI(uriNew)
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.expost1)
            }
        } else {
            //set it back to normal
            profileImage.setImageResource(R.drawable.expost1)
        }
    }
    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }


}