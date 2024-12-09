package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ProfileActivity : AppCompatActivity() {

    private lateinit var feedButton: Button
    private lateinit var cameraButton: Button
    private lateinit var username: TextView
    private lateinit var bio: TextView
    private lateinit var databaseHelper: UserDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private var posts = listOf<Post>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        feedButton = findViewById(R.id.nav_home)
        cameraButton = findViewById(R.id.nav_cam)
        databaseHelper = UserDatabaseHelper(this)
        username = findViewById(R.id.profileName)
        bio = findViewById(R.id.profileBio)
        recyclerView = findViewById(R.id.recyclerView)

        feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // username and posts stuff
        val userID: Int = requireNotNull(getCurrentUserId())
        posts = databaseHelper.getPostsByUserIds(listOf(userID))
        val usernameText = databaseHelper.getUsernameById(userID)
        username.text = usernameText

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = object : RecyclerView.Adapter<PostViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.post_profile, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
                val post = posts[position]
                if (post.imageUri.isNotEmpty()) {
                    holder.mainImage.visibility = View.VISIBLE
                    holder.mainImage.setImageURI(Uri.parse(post.imageUri))
                } else {
                    holder.mainImage.setImageResource(R.drawable.expost2)
                }
            }

            override fun getItemCount(): Int {
                return posts.size
            }
        }
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainImage: ImageView = view.findViewById(R.id.mainImage)
    }

}