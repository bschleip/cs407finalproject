package com.cs407.finalproject

import android.annotation.SuppressLint
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
import android.location.LocationManager
import com.google.android.gms.location.Priority


class  ConfirmationActivity : AppCompatActivity() {

    private lateinit var retakeButton: Button
    private lateinit var addInfoButton: Button
    private lateinit var postButton: Button
    private lateinit var confirmationImage: ImageView
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
            this.includeLocation = switchIncludeLocation.isChecked
            this.caption = captionEditText.text.toString()

            // TODO: add caption to the post

            if (includeLocation) {
                checkLocationPermissionAndGetLocation()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

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

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        Log.d("getLastLocation", "getLastLocation entry")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("getLastLocation", "got values: $latitude, $longitude")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConfirmationActivity", "Error getting location: ${e.message}")
            }
        Log.d("getLastLocation", "getLastLocation exit")
    }

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
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentUserId(): Int? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getInt("LOGGED_IN_USER_ID", -1).takeIf { it != -1 }
    }
}