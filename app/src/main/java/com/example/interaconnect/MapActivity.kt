package com.example.interaconnect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.view.GestureDetector
import android.view.MotionEvent
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MapActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var mapView: MapView
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var locationManager: LocationManager
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var lastMarker: GeoPoint? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize osmdroid
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_pref", Context.MODE_PRIVATE))

        // Initialize MapView
        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Set default location to Bogotá and zoom level
        val bogotaLocation = GeoPoint(4.610224, -74.085860) // Coordinates for Bogotá, Colombia
        mapView.controller.setCenter(bogotaLocation)
        mapView.controller.setZoom(15.0) // Set an appropriate zoom level

        // Initialize LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            enableMyLocation()
        }

        // Initialize light sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        // Register listener for the light sensor
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Create a GestureDetector to detect long presses
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onLongPress(e: MotionEvent) {
                e.let {
                    // Get the GeoPoint where the long press occurred
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        addMarker(geoPoint, address)
                        lastMarker = geoPoint
                        mapView.controller.setZoom(15.0) // Set a higher zoom level for better visibility
                        mapView.controller.setCenter(geoPoint) // Move camera to the location
                        // Calculate distance to the marker
                        calculateDistance(geoPoint)
                    }
                }
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return true // Handle single tap if needed
            }

        })

        // Set an OnTouchListener on the MapView to detect gestures
        mapView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        // Configure search box
        val searchBox: EditText = findViewById(R.id.searchBox)
        searchBox.setOnEditorActionListener { _, _, _ ->
            val location = searchBox.text.toString()
            if (location.isNotEmpty()) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(location, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val geoPoint = GeoPoint(address.latitude, address.longitude)
                    addMarker(geoPoint, location)
                    mapView.controller.setZoom(15.0) // Set a higher zoom level for better visibility
                    mapView.controller.setCenter(geoPoint) // Move camera to the location
                    lastMarker = geoPoint
                    calculateDistance(geoPoint)
                }
            }
            false
        }
    }

    // Method to enable the current location
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay = MyLocationNewOverlay(mapView)
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation()
            mapView.overlays.add(myLocationOverlay)

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, this)

            // Disable follow mode after the initial location is found
            myLocationOverlay?.runOnFirstFix {
                runOnUiThread {
                    myLocationOverlay?.disableFollowLocation() // Disable auto-follow
                }
            }
        }
    }

    private fun addMarker(geoPoint: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = title
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)

        // Explicitly center the map on the marker with a zoom level
        mapView.controller.setCenter(geoPoint)
        mapView.controller.setZoom(12.0)

        // Calculate distance to the marker
        calculateDistance(geoPoint)

        mapView.invalidate() // Refresh the map
    }




    private fun calculateDistance(markerLatLng: GeoPoint) {
        val myLocation = myLocationOverlay?.myLocation
        if (myLocation != null) {
            val currentLatLng = GeoPoint(myLocation.latitude, myLocation.longitude)
            val distance = currentLatLng.distanceToAsDouble(markerLatLng)
            Toast.makeText(this, "Distance to marker: ${distance.toInt()} meters", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                val lightLevel = it.values[0]
                if (lightLevel < 10) {
                    // Switch to dark mode if the light level is low
                    mapView.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                } else {
                    // Switch to light mode
                    mapView.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onLocationChanged(location: Location) {
        // Update the current location
        myLocationOverlay?.enableMyLocation()
        // No centering here; just keep updating the location
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        myLocationOverlay?.disableMyLocation()
    }
}
