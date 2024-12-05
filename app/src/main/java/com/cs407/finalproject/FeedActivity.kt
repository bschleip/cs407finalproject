package com.cs407.finalproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FeedActivity : AppCompatActivity() {

    private lateinit var cameraBtn: Button
    private lateinit var profileBtn: Button
    private lateinit var postDatabaseHelper: UserDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var noPostsText: TextView
    private lateinit var adapter: RecyclerView.Adapter<PostViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed)

        // Initialize the database helper
        postDatabaseHelper = UserDatabaseHelper(this)

        // Bind UI components
        cameraBtn = findViewById(R.id.nav_cam)
        profileBtn = findViewById(R.id.nav_profile)
        recyclerView = findViewById(R.id.recyclerView)
        noPostsText = findViewById(R.id.no_posts_text)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts from the database
        val posts = fetchPosts()

        // Check if there are no posts
        if (posts.isEmpty()) {
            noPostsText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noPostsText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Create and set the adapter
            adapter = createPostAdapter(posts)
            recyclerView.adapter = adapter
        }

        // Set navigation button listeners
        cameraBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onDestroy() {
        postDatabaseHelper.close() // Close the database
        super.onDestroy()
    }

    private fun fetchPosts(): List<Post> {
        return try {
            postDatabaseHelper.getAllPosts()
        } catch (e: Exception) {
            Log.e("FeedActivity", "Error fetching posts: ${e.message}")
            emptyList()
        }
    }

    private fun createPostAdapter(posts: List<Post>): RecyclerView.Adapter<PostViewHolder> {
        return object : RecyclerView.Adapter<PostViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
                val post = posts[position]
                // Load image safely
                if (post.imageUri != null) {
                    holder.postImage.visibility = View.VISIBLE
                    holder.postImage.setImageURI(Uri.parse(post.imageUri))
                } else {
                    holder.postImage.visibility = View.GONE
                }


                // Bind text data
                holder.captionText.text = post.caption ?: ""

                // Handle like button click
                holder.likeButton.setOnClickListener {
                    post.likes++
                    postDatabaseHelper.updateLikes(post.id, post.likes)
                    notifyItemChanged(position) // Refresh the item
                }
            }

            override fun getItemCount(): Int = posts.size
        }
    }
}

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val postImage: ImageView = view.findViewById(R.id.post_image)
    val likeButton: Button = view.findViewById(R.id.like_button)
    val captionText: TextView = view.findViewById(R.id.caption_text)
}
