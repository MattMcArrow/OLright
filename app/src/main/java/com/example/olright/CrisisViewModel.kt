package com.example.olright

import android.media.MediaPlayer
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class CrisisViewModel : ViewModel() {

    // Mutable flow to hold crisis data
    private val _crisises = MutableStateFlow<List<Crisis>>(emptyList())
    val crisises = _crisises.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                if (fetchNews()) {
                    fetchCrisises()
                }
                delay(5000)
            }
        }
    }

    // Fetch data from Supabase and update state
    fun fetchCrisises() {
        viewModelScope.launch {
            try {
                val results = supabase.from("crisis").select().decodeList<Crisis>()
                _crisises.value = results
                results.forEach { crisis ->
                    Log.d("CrisisViewModel", "Fetched crisis: ${crisis.crisis}")
                }
            } catch (e: Exception) {
                Log.e("CrisisViewModel", "Error fetching data: ${e.message}")
            }
        }
    }

    private suspend fun fetchNews(): Boolean {
        val geo = PointGeometrie(listOf(10.0, 10.0)) //TODO ZDE BUDE GET MY LOCATION
        return try {
            // Use async to get the result back
            val result = viewModelScope.async {
                supabase.postgrest.rpc("is_dangerous_location", geo).decodeAs<Boolean>()
            }
            result.await()  // Wait for the result
        } catch (e: Exception) {
            Log.e("CrisisViewModel", "Error fetching data: ${e.message}")
            false  // Return a default value on error
        }
    }

}
