package com.cs407.finalproject

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.TextureView
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.net.Uri
import android.widget.ImageButton
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var textureView: TextureView
    private lateinit var btnCapture: Button
    private var camera: Camera? = null
    private lateinit var feedButton: Button
    private lateinit var settingsBtn: ImageButton


    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureView = findViewById(R.id.textureView)
        btnCapture = findViewById(R.id.captureButton)
        feedButton = findViewById(R.id.feedButton)
        settingsBtn = findViewById(R.id.settingsButton)


        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            textureView.surfaceTextureListener = this
        }

        btnCapture.setOnClickListener {
            // Take a picture
            camera?.takePicture(null, null, Camera.PictureCallback { data, _ ->
                // Convert byte array to Bitmap
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                // Save the image and get the URI
                val imageUri = saveBitmap(bitmap)
                // Start PostConfirmationActivity with the image URI
                val intent = Intent(this, ConfirmationActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            })
        }

        feedButton.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)

        }
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        }

    }

    private fun startCamera() {
        try {
            camera = Camera.open()
            camera?.apply {
                setDisplayOrientation(90) // Rotate to portrait mode

                // Configure camera parameters
                val parameters = this.parameters
                val bestPreviewSize = parameters.supportedPreviewSizes
                    .maxByOrNull { it.width * it.height }
                bestPreviewSize?.let {
                    parameters.setPreviewSize(it.width, it.height)
                    this.parameters = parameters
                }

                // Set the SurfaceTexture as the preview display
                setPreviewTexture(textureView.surfaceTexture)
                startPreview()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Save bitmap to file and return the URI
    private fun saveBitmap(bitmap: Bitmap): Uri {
        val file = File(externalCacheDir, "captured_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    // TextureView.SurfaceTextureListener methods
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        startCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Handle size changes if necessary
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        // Stop and release the camera
        camera?.stopPreview()
        camera?.release()
        camera = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Optional: do something when the texture is updated
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            textureView.surfaceTextureListener = this
        }
    }
}

