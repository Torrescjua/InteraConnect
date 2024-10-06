package com.example.interaconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private var lastMarker: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Sensor de luz
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        // Registrar el listener para el sensor de luz
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Solicitar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        mMap.isMyLocationEnabled = true

        // LongClickListener para agregar un marcador
        mMap.setOnMapLongClickListener { latLng ->
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    mMap.addMarker(MarkerOptions().position(latLng).title(address))
                    lastMarker = latLng
                    Toast.makeText(this, "Marcador agregado: $address", Toast.LENGTH_SHORT).show()

                    // Calcular distancia si es necesario
                    calculateDistance(latLng)
                }
            }
        }

        // Configurar el cuadro de texto para buscar direcciones
        val searchBox: EditText = findViewById(R.id.searchBox)
        searchBox.setOnEditorActionListener { _, _, _ ->
            val location = searchBox.text.toString()
            if (location.isNotEmpty()) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(location, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        mMap.addMarker(MarkerOptions().position(latLng).title(location))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        lastMarker = latLng
                        calculateDistance(latLng)
                    }
                }
            }
            false
        }
    }

    private fun calculateDistance(markerLatLng: LatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastLocation = mMap.myLocation
            if (lastLocation != null) {
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val distance = SphericalUtil.computeDistanceBetween(currentLatLng, markerLatLng)
                Toast.makeText(this, "Distancia al marcador: ${distance.toInt()} metros", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                val lightLevel = it.values[0]
                if (lightLevel < 10) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_map_style))
                } else {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_map_style))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
