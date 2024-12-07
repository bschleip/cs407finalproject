package com.cs407.finalproject

import android.content.Intent
import android.location.Geocoder
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
import java.util.Locale

class FeedActivity : AppCompatActivity() {
    private lateinit var cameraBtn: Button
    private lateinit var profileBtn: Button
    private lateinit var postDatabaseHelper: UserDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var noPostsText: TextView
    private lateinit var adapter: RecyclerView.Adapter<PostViewHolder>

    private var posts = listOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed)

        postDatabaseHelper = UserDatabaseHelper(this)

        // Bind UI components
        cameraBtn = findViewById(R.id.nav_cam)
        profileBtn = findViewById(R.id.nav_profile)
        recyclerView = findViewById(R.id.recyclerView)
        noPostsText = findViewById(R.id.no_posts_text)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPosts()

        // Set navigation button listeners
        cameraBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        posts = fetchPosts()

        if (posts.isEmpty()) {
            noPostsText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noPostsText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter = createPostAdapter(posts)
            recyclerView.adapter = adapter
        }
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
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
                val post = posts[position]

                // Bind image
                if (!post.imageUri.isNullOrEmpty()) {
                    holder.postImage.visibility = View.VISIBLE
                    holder.postImage.setImageURI(Uri.parse(post.imageUri))
                } else {
                    holder.postImage.visibility = View.GONE
                }

                // Bind caption
                holder.captionText.text = post.caption ?: ""

                // Bind geotag
                if (post.latitude != null && post.longitude != null) {
                    holder.geotagText.visibility = View.VISIBLE
                    getAddressFromLocation(post.latitude, post.longitude) { address ->
                        holder.geotagText.text = "📍 $address"
                    }
                } else {
                    holder.geotagText.visibility = View.GONE
                }

                // Initialize like button state
                holder.likeButton.isSelected = false // Default to unliked (or adapt based on saved state)

                //THIS SHOWS LIKE COUNT FOR TESTING
                //holder.likeButton.text = "Like (${post.likes})"

                // Handle like button toggle
                holder.likeButton.setOnClickListener {
                    if (holder.likeButton.isSelected) {
                        // Unlike the post
                        post.likes--
                        holder.likeButton.isSelected = false
                    } else {
                        // Like the post
                        post.likes++
                        holder.likeButton.isSelected = true
                    }

                    // Update database and UI
                    postDatabaseHelper.updateLikes(post.id, post.likes)
                    holder.likeButton.text = "Like (${post.likes})"
                }
            }

            override fun getItemCount(): Int = posts.size
        }
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

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val postImage: ImageView = view.findViewById(R.id.mainImage)
    val likeButton: Button = view.findViewById(R.id.button_favorite)
    val captionText: TextView = view.findViewById(R.id.Caption)
    val geotagText: TextView = view.findViewById(R.id.Geotag)
}
