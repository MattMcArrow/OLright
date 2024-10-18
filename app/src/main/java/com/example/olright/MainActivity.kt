package com.example.olright

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.olright.ui.theme.OLrightTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.Serializable
import org.w3c.dom.Text
import java.time.OffsetDateTime
import android.util.Log

val supabase = createSupabaseClient(
    supabaseUrl = "https://iguijgpwqewgbywehkjc.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlndWlqZ3B3cWV3Z2J5d2Voa2pjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjkyNzU2NTcsImV4cCI6MjA0NDg1MTY1N30.tlr9SPZaqrsfVJ_eUXPi0IACfqmGBhqA7caj8nSojTQ"
) {
    install(Postgrest)
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OLrightTheme {
                Surface (
                    modifier=Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    ListDb()
                }
            }
        }
    }
}

@Serializable
data class Crisis (
    val id: Int,
    val crisis: String,
    val description: String,
    val timeStart: String,
    val timeEnd: String,
    val geom: String
)

@Composable
fun ListDb() {
    val crisises = remember { mutableListOf<Crisis>() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            val results = supabase.from("crisis").select().decodeList<Crisis>()
            crisises.addAll(results)
        }
    }
    println("Crisis ID:")
    Log.d("TAG", "message")
    LazyColumn {
        items(crisises){
            crisis -> ListItem(headlineContent = {Text(text=crisis.crisis)})
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    OLrightTheme {
        ListDb()
    }
}