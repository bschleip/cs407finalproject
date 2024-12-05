package com.cs407.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class FeedActivity : AppCompatActivity() {

    private lateinit var cameraBtn: Button
    private lateinit var profileBtn: Button
    private lateinit var postDatabaseHelper: UserDatabaseHelper
    private lateinit var recyclerView: RecyclerView
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

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts from the database
        val posts = postDatabaseHelper.getAllPosts()

        // Create and set the adapter
        adapter = createPostAdapter(posts)
        recyclerView.adapter = adapter

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

    private fun createPostAdapter(posts: List<Post>): RecyclerView.Adapter<PostViewHolder> {
        return object : RecyclerView.Adapter<PostViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
                val post = posts[position]
                // Load image using Glide
                Glide.with(holder.itemView.context)
                    .load(post.imageUri)
//                    .placeholder(R.drawable.placeholder_image) // A placeholder while loading
                    .into(holder.postImage)

                // Bind text data
//                holder.geotagText.text = post.geotag ?: "Unknown Location"
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
    val geotagText: TextView = view.findViewById(R.id.geotag_text)
    val captionText: TextView = view.findViewById(R.id.caption_text)
}

//class FeedActivity : AppCompatActivity() {
//
//    private lateinit var cameraBtn: Button
//    private lateinit var profileBtn: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_post_feed)
//
//        cameraBtn = findViewById(R.id.nav_cam)
//        profileBtn = findViewById(R.id.nav_profile)
//
//        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
//        val posts = createPosts()
//
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = createPostAdapter(posts)
//
//        cameraBtn.setOnClickListener {
//            val intent = Intent(this, CameraActivity::class.java)
//            startActivity(intent)
//        }
//        profileBtn.setOnClickListener {
//            startActivity(Intent(this, ProfileActivity::class.java))
//        }
//    }
//
//}
//
//private fun createPosts(): List<Post> {
//    val images = arrayOf(R.drawable.exsotd, R.drawable.expost1, R.drawable.expost2)
//    val captions = arrayOf("Shenanigan of the Day", "First post", "Second post")
//    val geotags = arrayOf("Location SOTD", "Location 1", "Location 2")
//
//    return images.indices.map { i ->
//        Post(images[i], captions[i], geotags[i])
//    }
//}
//
//private fun createPostAdapter(posts: List<Post>): RecyclerView.Adapter<PostViewHolder> {
//    return object : RecyclerView.Adapter<PostViewHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
//            return PostViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//            val post = posts[position]
//            holder.postImage.setImageResource(post.imageResId)
//            holder.geotagText.text = post.geotag
//            holder.captionText.text = post.caption
//
//            holder.likeButton.setOnClickListener {
//                // Handle like button click
//            }
//        }
//
//        override fun getItemCount(): Int = posts.size
//    }
//}
//
//
//class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//    val postImage: ImageView = view.findViewById(R.id.post_image)
//    val likeButton: Button = view.findViewById(R.id.like_button)
//    val geotagText: TextView = view.findViewById(R.id.geotag_text)
//    val captionText: TextView = view.findViewById(R.id.caption_text)
//}
