package com.cs407.finalproject

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class PostHandler : AppCompatActivity() {
    private lateinit var buttonFavorite: ToggleButton
    private lateinit var scaleAnimation: ScaleAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        buttonFavorite = findViewById(R.id.button_favorite)

        setupAnimation()

        // Setup click listener
        buttonFavorite.setOnCheckedChangeListener { buttonView, isChecked ->

            // Start animation
            buttonView.startAnimation(scaleAnimation)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                buttonView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            //Update like count or perform other actions
            updateLikeCount(isChecked)
        }

    }

    private fun setupAnimation() { //scaling animation
        scaleAnimation = ScaleAnimation(
            0.7f, // x scale
            1.0f,
            0.7f, // y scale
            1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500 // Animation duration in milliseconds
            interpolator = BounceInterpolator() // Bounce effect
        }
    }

    private var lastTapTime: Long = 0

    private fun updateLikeCount(isLiked: Boolean) {
        // TODO implement counting logic
    }
}