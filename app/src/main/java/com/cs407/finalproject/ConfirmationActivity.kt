package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import android.util.Log


class  ConfirmationActivity : AppCompatActivity() {

    private lateinit var retakeButton: Button
    private lateinit var addInfoButton: Button
    private lateinit var postButton: Button
    private lateinit var confirmationImage: ImageView
    private lateinit var userDatabaseHelper: UserDatabaseHelper

    private var imageUri: Uri? = null
    private var caption: String? = null
    private var includeLocation: Boolean = false
    private var location: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        userDatabaseHelper = UserDatabaseHelper(this)

        imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }

        retakeButton = findViewById(R.id.retakeButton)
        addInfoButton = findViewById(R.id.addInfoButton)
        postButton = findViewById(R.id.postButton)
        confirmationImage = findViewById(R.id.confirmationImage)

        imageUri?.let {
            confirmationImage.setImageURI(it)
        }

        retakeButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        addInfoButton.setOnClickListener { showAddInfoDialog() }

        postButton.setOnClickListener { savePostToDatabase() }
    }

    private fun showAddInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_info, null)

        val switchIncludeLocation = dialogView.findViewById<SwitchCompat>(R.id.switchIncludeLocation)
        val captionEditText = dialogView.findViewById<EditText>(R.id.captionEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val includeLocation = switchIncludeLocation.isChecked
            val caption = captionEditText.text.toString()

            // TODO: add caption to the post

            if (includeLocation) {
                // TODO: location services
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun savePostToDatabase() {
        if (imageUri == null) {
            // TODO: error message; no image
            Log.e("ConfirmationActivity", "imageUri is null")
            return
        }

        val userId = getCurrentUserId()
        if (userId == null) {
            // TODO: error; no user ID found
            Log.e("ConfirmationActivity", "userId is null")
            return
        } else {
            val postId = userDatabaseHelper.addPost(
                userId = userId,
                imageUri = imageUri.toString(),
                caption = caption
            )

            if (postId != -1L) {
                // post saved
                startActivity(Intent(this, FeedActivity::class.java))
            } else {
                // TODO: something went wrong
                Log.e("ConfirmationActivity", "postId is bad")
            }
        }
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }
}