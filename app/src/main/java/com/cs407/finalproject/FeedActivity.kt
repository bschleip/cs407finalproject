package com.cs407.shenanigans

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.finalproject.R

class FeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val images = arrayOf(R.drawable.exsotd, R.drawable.expost1, R.drawable.expost2)
        val captions = arrayOf("Shenanigan of the Day", "First post", "Second post")
        val geotags = arrayOf("Location SOTD", "Location 1", "Location 2")

        val posts = mutableListOf<Post>()
        for (i in images.indices) {
            posts.add(Post(images[i], captions[i], geotags[i]))
        }

        val sotd = posts[0]
        val remainingPosts = posts.drop(1)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PostAdapter(listOf(sotd) + remainingPosts)
    }

    private inner class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

        inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val postImage: ImageView = view.findViewById(R.id.post_image)
            val likeButton: Button = view.findViewById(R.id.like_button)
            val commentButton: Button = view.findViewById(R.id.comment_button)
            val geotagText: TextView = view.findViewById(R.id.geotag_text)
            val captionText: TextView = view.findViewById(R.id.caption_text)
            val commentInput: EditText = view.findViewById(R.id.comment_input)
            val commentDisplay: TextView = view.findViewById(R.id.comment_display)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
            return PostViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val post = posts[position]

            holder.postImage.setImageResource(post.imageResId)
            holder.geotagText.text = post.geotag
            holder.captionText.text = post.caption

            holder.commentButton.setOnClickListener {
                if (holder.commentInput.visibility == View.GONE) {
                    holder.commentInput.visibility = View.VISIBLE
                    holder.commentInput.requestFocus()
                } else {
                    holder.commentInput.visibility = View.GONE
                }
            }

            holder.commentInput.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    val commentText = holder.commentInput.text.toString().trim()
                    if (commentText.isNotEmpty()) {
                        holder.commentDisplay.text = commentText
                        holder.commentDisplay.visibility = View.VISIBLE
                    }
                    holder.commentInput.visibility = View.GONE
                    holder.commentInput.text.clear()
                    true
                } else {
                    false
                }
            }

            holder.likeButton.setOnClickListener {
                // Handle like button click
            }
        }

        override fun getItemCount(): Int = posts.size
    }

    data class Post(val imageResId: Int, val caption: String, val geotag: String)
}
