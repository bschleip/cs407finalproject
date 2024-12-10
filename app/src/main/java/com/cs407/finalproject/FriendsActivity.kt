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

        // Set up RecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Fetch friends for the current user
        val friendsList = fetchFriends()
        if (friendsList.isEmpty()) {
            Toast.makeText(this, "No friends found!", Toast.LENGTH_SHORT).show()
        }

        friendsRecyclerView.adapter = FriendsAdapter(friendsList.toMutableList())

        friendsBackButton.setOnClickListener { finish() }
    }


    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    private fun fetchFriends(): List<String> {
        val currentUserId = getCurrentUserId()
        val friendIds = currentUserId?.let { databaseHelper.getFriends(it) }
        if (friendIds != null) {
            return friendIds.map { friendId -> databaseHelper.getUsernameById(friendId) }
        }
        return emptyStringList
    }

    private inner class FriendsAdapter(private var friends: MutableList<String>) :
        RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friends, parent, false)
            return FriendsViewHolder(view)
        }

        override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
            holder.bind(friends[position])
        }

        override fun getItemCount(): Int = friends.size

        inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val friendNameTextView: TextView = itemView.findViewById(R.id.friend_name)
            private val removeFriendButton: Button = itemView.findViewById(R.id.remove_friend_button)

            fun bind(friendName: String) {
                friendNameTextView.text = friendName

                removeFriendButton.setOnClickListener {
                    showRemoveFriendDialog(friendName)
                }
            }
        }

        private fun showRemoveFriendDialog(friendName: String) {
            val dialog = android.app.AlertDialog.Builder(this@FriendsActivity)
                .setTitle("Remove Friend")
                .setMessage("Are you sure you want to remove $friendName from your friends list?")
                .setPositiveButton("Yes") { _, _ ->
                    removeFriend(friendName)
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
                Toast.makeText(this@FriendsActivity, "$friendName removed from friends list.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@FriendsActivity, "Failed to remove $friendName.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
