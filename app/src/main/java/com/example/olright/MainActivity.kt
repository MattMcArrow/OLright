package com.example.olright

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Serializable
data class Geometrie(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

@Serializable
data class PostgrePoint(
    val coordinates: List<Double>,
    val type: String
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
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var polygonsOverlay: FolderOverlay
    private lateinit var greenview: LinearLayout
    private lateinit var crisisview: LinearLayout
    private lateinit var btnkill: Button
    private lateinit var descriptionview: TextView
    private lateinit var crisisnameview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }



        //makeSound()
        setContent {
            GreenBackgroundScreen()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.loudsound)

        setContentView(R.layout.activity_main)
        greenview = findViewById(R.id.greenview)
        crisisview = findViewById(R.id.crisislayout)
        greenview = findViewById(R.id.greenview)
        descriptionview = findViewById(R.id.popis)
        crisisnameview = findViewById(R.id.crisis)
        btnkill = findViewById(R.id.btn)
        btnkill.setOnClickListener {
            stopSound()
        }
        crisisview.visibility = android.view.View.INVISIBLE


        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        val mapController = map.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(49.5948, 17.241)
        mapController.setCenter(startPoint)
        val locationProvider = GpsMyLocationProvider(this)
        locationOverlay = MyLocationNewOverlay(locationProvider, map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        viewModel = CrisisViewModel()


        Log.e("wrfsfd", viewModel.crisises.toString())





        lifecycleScope.launch {
            var currpolygon: Crisis? = null
            while(true){
                if (locationOverlay.myLocation != null) {
                    val isIntersected = viewModel.fetchNews(locationOverlay.myLocation)

                    viewModel.fetchCrisises()
                    if (::polygonsOverlay.isInitialized) {
                        map.overlays.remove(polygonsOverlay)
                    }
                    polygonsOverlay = viewModel.getPolygonsOverlay()
                    map.overlays.add(polygonsOverlay)

                    if (isIntersected != 0 ) {
                        for (crisis in viewModel.crisises.value) {
                            if (crisis.id == isIntersected) {
                                currpolygon = crisis
                            }
                            //polygonsOverlay.items
                        }
                        if (currpolygon != null) {
                            descriptionview.setText(currpolygon.description)
                            crisisnameview.setText(currpolygon.crisis)
                        }
                        crisisview.visibility = android.view.View.VISIBLE
                        greenview.visibility = android.view.View.INVISIBLE

                        makeSound()
                    }
                    else{
                        crisisview.visibility = android.view.View.INVISIBLE
                        greenview.visibility = android.view.View.VISIBLE
                    }



                }
                delay(5000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
        locationOverlay.enableMyLocation()
        //locationOverlay.enableFollowLocation()
    }

    override fun onPause() {
        super.onPause()

        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
        locationOverlay.disableMyLocation()
        locationOverlay.disableFollowLocation()
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

    fun makeSound(){

        mediaPlayer.isLooping = true // Set to true if you want it to loop
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.start()
    }

    fun stopSound(){
        mediaPlayer.stop()
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



@Composable
fun GreenBackgroundScreen() {
    // Main layout with green background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(153, 204, 0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "GPS aktivní\n\n\n",
                fontSize = 36.sp,
                color = Color.White,
                modifier = Modifier.padding(10.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ŽÁDNÁ HROZBA\nV OKOLÍ\n",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PNG Image (place it in res/drawable folder)
            Image(
                painter = painterResource(id = R.drawable.ikona_zelena),  // Replace with your PNG resource
                contentDescription = "Sample Image",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}












/*

  setContent {
                GreenBackgroundScreen()
            }
  setContent {
    GreenBackgroundScreen()
}


@Composable
    fun SoundAppUI() {
        // UI with a button to start the sound service
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { startSoundService() }) {
                Text("Start Sound Service")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { stopSoundService() }) {
                Text("Stop Sound Service")
            }
        }
            fun startSoundService() {
        val intent = Intent(this, SoundService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "KLIK", Toast.LENGTH_SHORT).show()
    }
    fun stopSoundService() {
        val intent = Intent(this, SoundService::class.java)
        stopService(intent)
    }
    }*/