package com.example.interaconnect

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia

class CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button

    private var imageUri: Uri? = null

    // Launcher para abrir el selector de fotos (PhotoPicker) en Android 14+
    private val photoPickerLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let {
            imageView.setImageURI(it)
        }
    }

    // Launcher para abrir la galería en Android 13 y anteriores
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
        }
    }

    // Launcher para abrir la cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Mostrar la imagen guardada desde la URI
            imageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imageView = findViewById(R.id.imageView)
        btnGallery = findViewById(R.id.btn_gallery)
        btnCamera = findViewById(R.id.btn_camera)

        btnGallery.setOnClickListener {
            if (hasStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        btnCamera.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ usa PhotoPicker
            photoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        } else {
            // Android 13 y versiones anteriores
            galleryLauncher.launch("image/*")
        }
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        }

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33)
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10, 11, 12
            true // No se necesita READ_EXTERNAL_STORAGE en Android 10 y superiores (uso de Scoped Storage)
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAMERA)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_IMAGE_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission is required to access the gallery", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAMERA = 2
        private const val REQUEST_IMAGE_STORAGE = 3
    }
}
