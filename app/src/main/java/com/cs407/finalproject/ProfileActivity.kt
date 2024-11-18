package com.cs407.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var feedButton: Button
    private lateinit var cameraButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        feedButton = findViewById(R.id.nav_home)
        cameraButton = findViewById(R.id.nav_cam)

        feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }


}