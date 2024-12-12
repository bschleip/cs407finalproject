package com.cs407.finalproject

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FriendsActivity : AppCompatActivity() {

    private lateinit var friendsBackButton: View
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var databaseHelper: UserDatabaseHelper

    private var emptyStringList = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        databaseHelper = UserDatabaseHelper(this)

        friendsBackButton = findViewById(R.id.friends_back_button)
        friendsRecyclerView = findViewById(R.id.friends_recycler_view)


        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        val friendsList = fetchFriends()
        if (friendsList.isEmpty()) {
            Toast.makeText(this, "No friends found!", Toast.LENGTH_SHORT).show()
        }

        val adapter = createFriendsAdapter(friendsList.toMutableList())
        friendsRecyclerView.adapter = adapter

        friendsBackButton.setOnClickListener { finish() }
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    private fun fetchFriends(): List<String> {
        val currentUserId = getCurrentUserId()
        val friendIds = currentUserId?.let { databaseHelper.getFriends(it) }
        return friendIds?.map { friendId ->
            databaseHelper.getUsernameById(friendId)
        } ?: emptyStringList
    }

    private fun createFriendsAdapter(friends: MutableList<String>): RecyclerView.Adapter<FriendsViewHolder> {
        return object : RecyclerView.Adapter<FriendsViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friends, parent, false)
                return FriendsViewHolder(view)
            }

            override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
                val friendName = friends[position]
                holder.friendNameTextView.text = friendName

                holder.removeFriendButton.setOnClickListener {
                    showRemoveFriendDialog(friendName) {
                        removeFriend(friendName)
                    }
                }
            }

            override fun getItemCount(): Int = friends.size

            private fun showRemoveFriendDialog(friendName: String, onConfirm: () -> Unit) {
                val dialog = android.app.AlertDialog.Builder(this@FriendsActivity)
                    .setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove $friendName from your friends list?")
                    .setPositiveButton("Yes") { _, _ ->
                        onConfirm()
                    }
                    .setNegativeButton("No", null)
                    .create()

                dialog.show()
            }

            private fun removeFriend(friendName: String) {
                val userId = getCurrentUserId()
                if (userId != null) {
                    databaseHelper.removeFriend(userId, friendName)
                    friends.remove(friendName)
                    notifyDataSetChanged()
                    Toast.makeText(
                        this@FriendsActivity,
                        "$friendName removed from friends list.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@FriendsActivity,
                        "Failed to remove $friendName.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val friendNameTextView: TextView = itemView.findViewById(R.id.friend_name)
    val removeFriendButton: Button = itemView.findViewById(R.id.remove_friend_button)
}
