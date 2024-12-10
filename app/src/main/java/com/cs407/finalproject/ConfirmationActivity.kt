package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.view.View
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback


class  ConfirmationActivity : AppCompatActivity() {

    private lateinit var retakeButton: Button
    private lateinit var addInfoButton: Button
    private lateinit var postButton: Button
    private lateinit var confirmationImage: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var userDatabaseHelper: UserDatabaseHelper

    private var imageUri: Uri? = null
    private var caption: String? = null

    // Location handling
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var includeLocation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        userDatabaseHelper = UserDatabaseHelper(this)

        imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }

        retakeButton = findViewById(R.id.retakeButton)
        addInfoButton = findViewById(R.id.addInfoButton)
        postButton = findViewById(R.id.postButton)
        confirmationImage = findViewById(R.id.confirmationImage)
        progressBar = findViewById(R.id.progressBar)

        imageUri?.let { confirmationImage.setImageURI(it) }

        retakeButton.setOnClickListener { showDiscardDialog() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { showDiscardDialog() }
        })

        addInfoButton.setOnClickListener { showAddInfoDialog() }

        postButton.setOnClickListener { savePostToDatabase() }
    }

    private fun showDiscardDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Post?")
            .setMessage("Are you sure you want to go back? Your post will be discarded.")
            .setPositiveButton("Discard") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_info, null)

        val switchIncludeLocation = dialogView.findViewById<SwitchCompat>(R.id.switchIncludeLocation)
        val captionEditText = dialogView.findViewById<EditText>(R.id.captionEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        switchIncludeLocation.isChecked = includeLocation
        caption?.let { captionEditText.setText(it) }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            this.includeLocation = switchIncludeLocation.isChecked
            this.caption = captionEditText.text.toString()
            if (includeLocation) { checkLocationPermissionAndGetLocation() }
            dialog.dismiss()
        }
        dialog.show()
    }

    // Location methods
    private fun checkLocationPermissionAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                includeLocation = false
            }
        }

    private fun getLastLocation() {
        progressBar.visibility = View.VISIBLE

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ConfirmationActivity", "Location permission not granted")
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            includeLocation = false
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                progressBar.visibility = View.GONE
                if (location == null) {
                    Toast.makeText(this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show()
                    includeLocation = false
                    return@addOnSuccessListener
                }
                latitude = location.latitude
                longitude = location.longitude
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("ConfirmationActivity", "Error getting location: ${e.message}")
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                includeLocation = false
            }
    }

    // Post saving
    private fun savePostToDatabase() {

        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val postId = userDatabaseHelper.addPost(
            userId = userId,
            imageUri = imageUri.toString(),
            caption = caption,
            latitude = if (includeLocation) latitude else null,
            longitude = if (includeLocation) longitude else null
        )

        if (postId != -1L) {
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            progressBar.visibility = View.GONE
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }

    // QOL methods
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("imageUri", imageUri?.toString())
        outState.putString("caption", caption)
        outState.putBoolean("includeLocation", includeLocation)
        outState.putDouble("latitude", latitude ?: 0.0)
        outState.putDouble("longitude", longitude ?: 0.0)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        imageUri = savedInstanceState.getString("imageUri")?.let { Uri.parse(it) }
        caption = savedInstanceState.getString("caption")
        includeLocation = savedInstanceState.getBoolean("includeLocation")
        latitude = savedInstanceState.getDouble("latitude").takeIf { it != 0.0 }
        longitude = savedInstanceState.getDouble("longitude").takeIf { it != 0.0 }
    }
}