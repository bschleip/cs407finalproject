package com.cs407.finalproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var captureButton: Button
    private lateinit var feedButton: Button
    private lateinit var profileButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var addFriendsButton: ImageButton
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if a user is logged in
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("LOGGED_IN_USER_ID", -1)

        if (userId == -1) {
            // No user is logged in; redirect to SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        captureButton = findViewById(R.id.captureButton)
        feedButton = findViewById(R.id.feedButton)
        profileButton = findViewById(R.id.profileButton)
        settingsButton = findViewById(R.id.settingsButton)
        addFriendsButton = findViewById(R.id.addFriendsButton)

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureButton.setOnClickListener { takePhoto() }
        feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addFriendsButton.setOnClickListener {
            showAddFriendsDialog()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Log.e("CameraActivity", "Camera permission denied")
        }
    }

    private fun showAddFriendsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_friends, null)

        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)
        val addFriendEditText = dialogView.findViewById<EditText>(R.id.addFriendsEditText)
        val addFriendButton = dialogView.findViewById<Button>(R.id.addFriendButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        addFriendButton.setOnClickListener {
            val friendUsername = addFriendEditText.text.toString().trim()
            val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("LOGGED_IN_USER_ID", -1)

            if (friendUsername.isEmpty()) {
                Toast.makeText(this, "Please enter a username.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == -1) {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val dbHelper = UserDatabaseHelper(this)
            val success = dbHelper.addFriend(userId, friendUsername)

            if (success) {
                Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add friend. User may not exist or is already a friend.", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraActivity", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_${timeStamp}.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, imageFileName)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val intent = Intent(this@CameraActivity, ConfirmationActivity::class.java)
                    intent.putExtra("imageUri", savedUri.toString())
                    startActivity(intent)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }
}
