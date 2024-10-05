package com.example.interaconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar los botones
        initializeButtons()
    }

    // Función para inicializar los botones y configurar los OnClickListeners
    private fun initializeButtons() {
        val btnContacts: ImageButton = findViewById(R.id.btn_contacts)
        val btnCamera: ImageButton = findViewById(R.id.btn_camera)
        val btnMap: ImageButton = findViewById(R.id.btn_map)

        // Configurar eventos OnClickListener
        btnContacts.setOnClickListener { openContactActivity() }
        btnCamera.setOnClickListener { openCameraActivity() }
        btnMap.setOnClickListener { openMapActivity() }
    }

    // Función para abrir la actividad de Contactos
    private fun openContactActivity() {
        val intent = Intent(this, ContactActivity::class.java)
        startActivity(intent)
    }

    // Función para abrir la actividad de Cámara
    private fun openCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    // Función para abrir la actividad de Mapa
    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }
}
