package com.example.olright

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint


class CrisisViewModel : ViewModel() {

    // Mutable flow to hold crisis data
    val crisises = MutableStateFlow<List<Crisis>>(emptyList())


    init {

        viewModelScope.launch {
            while (true) {
                fetchCrisises()
                delay(5000)
            }
        }
    }

    fun collectCrisisData() {
        CoroutineScope(Dispatchers.Main).launch {
            crisises.collect { crisisList ->

                println("Collected Crises: $crisisList")
            }
        }
    }

    fun fetchCrisises() {
        viewModelScope.launch {
            try {
                val results = supabase.from("crisis").select().decodeList<Crisis>()
                crisises.value = results
            } catch (e: Exception) {
                Log.e("CrisisViewModel", "Error fetching data: ${e.message}")
            }
        }
    }
    @Serializable
    data class Point (val lat:Double, val lon:Double)

    suspend fun fetchNews(myLocation: GeoPoint): Boolean {
        val lat = myLocation.latitude
        val lon = myLocation.longitude
        var point = PostgrePoint(listOf(lon, lat), "Point")
        try {
            // Use async to get the result back
            val result = viewModelScope.async {
                val p = Point(lat, lon);
                supabase.postgrest.rpc("is_dangerous_location2", p).decodeAs<Boolean>()
            }
            val test = result.await()
            return test
        } catch (e: Exception) {
            Log.e("blabla", "Error fetching data: ${e.message}")
            return false  // Return a default value on error
        }
    }


}
