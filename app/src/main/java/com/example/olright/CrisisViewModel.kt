package com.example.olright

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

    suspend fun fetchNews(myLocation: GeoPoint): Boolean {

        return try {
            // Use async to get the result back
            val result = viewModelScope.async {
                supabase.postgrest.rpc("is_dangerous_location", myLocation).decodeAs<Boolean>()
            }
            result.await()  // Wait for the result
        } catch (e: Exception) {
            Log.e("CrisisViewModel", "Error fetching data: ${e.message}")
            false  // Return a default value on error
        }
    }


}
