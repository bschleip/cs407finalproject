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

                if (post.latitude != null && post.longitude != null) {
                    holder.locationText.visibility = View.VISIBLE

                    // To show raw coordinates:
//                    holder.locationText.text = "📍 ${post.latitude}, ${post.longitude}"

                    // Showing the actual address:
                    getAddressFromLocation(post.latitude, post.longitude) { address ->
                        holder.locationText.text = "📍 $address"
                    }
                } else {
                    holder.locationText.visibility = View.GONE
                }

                try {
                    if (post.imageUri.isNotEmpty()) {
                        holder.postImage.visibility = View.VISIBLE
                        val uri = Uri.parse(post.imageUri)
                        holder.postImage.setImageURI(uri)
                    } else {
                        Log.e("FeedActivity", "Empty imageUri for post: $post")
                        holder.postImage.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("FeedActivity", "Error loading image for post: $post", e)
                    holder.postImage.visibility = View.GONE
                }

                holder.likeButton.setOnClickListener {
                    post.likes++
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
    val postImage: ImageView = view.findViewById(R.id.post_image)
    val likeButton: Button = view.findViewById(R.id.like_button)
    val captionText: TextView = view.findViewById(R.id.caption_text)
    val locationText: TextView = view.findViewById(R.id.geotag_text)
}
