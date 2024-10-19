package com.example.olright

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
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
                fetchCrisises()
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
}
