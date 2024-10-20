package com.example.olright

import android.graphics.Paint
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Polygon


class CrisisViewModel : ViewModel() {

    // Mutable flow to hold crisis data
    val crisises = MutableStateFlow<List<Crisis>>(emptyList())


    init {

        viewModelScope.launch {
            while (true) {
                fetchCrisises()
                delay(10000)
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

    fun makePolygon(crisis: Crisis): Polygon {
        val polygon = Polygon()
        val points = mutableListOf<GeoPoint>()
        for (point in crisis.geom.coordinates[0]) {
            points.add(GeoPoint(point[1], point[0]))
        }
        polygon.points = points
        polygon.title = crisis.crisis
        polygon.id = crisis.id.toString()
        polygon.fillPaint.color = Color(228,29,55,100).toArgb()
        polygon.outlinePaint.color = Color(228,29,55,255).toArgb()
        return polygon
    }

    fun getPolygonsOverlay(): FolderOverlay{
        val folderOverlay = FolderOverlay()
        for (crisis in crisises.value) {
            val polygon = makePolygon(crisis)
            folderOverlay.add(polygon)
        }
        return folderOverlay
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

    suspend fun fetchNews(myLocation: GeoPoint): Int {
        val lat = myLocation.latitude
        val lon = myLocation.longitude
        var point = PostgrePoint(listOf(lon, lat), "Point")
        try {
            // Use async to get the result back
            val result = viewModelScope.async {
                val p = Point(lat, lon);
                supabase.postgrest.rpc("is_dangerous_location3", p).decodeAs<Int>()
            }
            val test = result.await()
            return test
        } catch (e: Exception) {
            Log.e("blabla", "Error fetching data: ${e.message}")
            return 0  // Return a default value on error
        }
    }


}
