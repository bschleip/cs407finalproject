package com.cs407.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat

class  ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val imageView: ImageView = findViewById(R.id.confirmationImage)
        val imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }

        imageUri?.let {
            imageView.setImageURI(it)
        }

        val retakeButton = findViewById<Button>(R.id.retakeButton)
        retakeButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        val addInfoButton = findViewById<Button>(R.id.addInfoButton)
        addInfoButton.setOnClickListener {
            showAddInfoDialog()
        }
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
            // TODO: handle the data saving
            dialog.dismiss()
        }

        dialog.show()
    }
}