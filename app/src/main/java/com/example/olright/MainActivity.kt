package com.example.olright

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Serializable
data class Geometrie(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

@Serializable
data class Crisis(
    val id: Int,
    val crisis: String,
    val description: String,
    val time_start: String,
    val time_end: String,
    val geom: Geometrie
)


val supabase = createSupabaseClient(
    supabaseUrl = "https://iguijgpwqewgbywehkjc.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlndWlqZ3B3cWV3Z2J5d2Voa2pjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjkyNzU2NTcsImV4cCI6MjA0NDg1MTY1N30.tlr9SPZaqrsfVJ_eUXPi0IACfqmGBhqA7caj8nSojTQ"
) {
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CrisisViewModel
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        viewModel = ViewModelProvider(this).get(CrisisViewModel::class.java)
        setContentView(R.layout.activity_main)

        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            enableMyLocation()
        }

        val mapController = map.controller
        mapController.setZoom(13)
        val startPoint = GeoPoint(49.5948, 17.241);
        mapController.setCenter(startPoint);
        //ListDb(viewModel)
        viewModel.fetchCrisises()
    }

    private fun enableMyLocation() {
        val locationProvider = GpsMyLocationProvider(this)
        locationOverlay = MyLocationNewOverlay(locationProvider, map)
        locationOverlay.enableMyLocation() // Starts tracking user’s location

        // Optionally, move the map to the current location when the location is found
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    map.controller.setCenter(myLocation)
                    map.controller.setZoom(18.0)  // Zoom in to user's location
                }
            }
        }
        map.overlays.add(locationOverlay)  // Add the overlay to the map
    }

    override fun onResume() {
        super.onResume()
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
        if (::locationOverlay.isInitialized) {
            locationOverlay.enableMyLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
        if (::locationOverlay.isInitialized) {
            locationOverlay.disableMyLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}




@Composable
fun ListDb(viewModel: CrisisViewModel) {
    // Collect the crisis data from ViewModel as state
    val crisises by viewModel.crisises.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCrisises()
    }

    LazyColumn {
        items(crisises) { crisis ->
            ListItem(
                headlineContent = { Text(text = crisis.crisis) },
                supportingContent = {
                    Column {
                        Text(text = "ID: ${crisis.id}")
                        Text(text = "Description: ${crisis.description}")
                        Text(text = "Start Time: ${crisis.time_start}")
                        Text(text = "End Time: ${crisis.time_end}")
                        Text(text = "Geometry: ${crisis.geom}")
                    }
                }
            )
        }
    }
}

