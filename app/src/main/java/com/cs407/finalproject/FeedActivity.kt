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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed)

        cameraBtn = findViewById(R.id.nav_cam)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val posts = createPosts()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = createPostAdapter(posts)

        cameraBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

}

private fun createPosts(): List<Post> {
    val images = arrayOf(R.drawable.exsotd, R.drawable.expost1, R.drawable.expost2)
    val captions = arrayOf("Shenanigan of the Day", "First post", "Second post")
    val geotags = arrayOf("Location SOTD", "Location 1", "Location 2")

    return images.indices.map { i ->
        Post(images[i], captions[i], geotags[i])
    }
}

private fun createPostAdapter(posts: List<Post>): RecyclerView.Adapter<PostViewHolder> {
    return object : RecyclerView.Adapter<PostViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
            return PostViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val post = posts[position]
            holder.postImage.setImageResource(post.imageResId)
            holder.geotagText.text = post.geotag
            holder.captionText.text = post.caption

            holder.likeButton.setOnClickListener {
                // Handle like button click
            }
        }

        override fun getItemCount(): Int = posts.size
    }
}


class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val postImage: ImageView = view.findViewById(R.id.post_image)
    val likeButton: Button = view.findViewById(R.id.like_button)
    val geotagText: TextView = view.findViewById(R.id.geotag_text)
    val captionText: TextView = view.findViewById(R.id.caption_text)
}

class Post(val imageResId: Int, val caption: String, val geotag: String)
