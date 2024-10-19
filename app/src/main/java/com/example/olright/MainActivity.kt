package com.example.olright

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.olright.ui.theme.OLrightTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        viewModel = ViewModelProvider(this).get(CrisisViewModel::class.java)
        setContent {
            OLrightTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListDb(viewModel)
                }
            }
        }
        viewModel.fetchCrisises() // Initial
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






//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun GreetingPreview() {
//    OLrightTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            ListDb()
//        }
//    }
//}


//@Composable
//fun ListDbPreview() {
//    val sampleCrisisList = listOf(
//        Crisis(1, "Sample Crisis 1", "Description 1", "2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z", Geometrie("", listOf(4.6878))),
//        Crisis(2, "Sample Crisis 2", "Description 2", "2024-02-01T00:00:00Z", "2024-02-02T00:00:00Z", Geometrie("", listOf(4.6878)))
//    )
//    LazyColumn {
//        items(sampleCrisisList) { crisis ->
//            ListItem(headlineContent = { Text(text = crisis.crisis) })
//        }
//    }
//}