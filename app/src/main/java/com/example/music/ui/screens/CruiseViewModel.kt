package com.example.music.ui.screens

import androidx.lifecycle.ViewModel
import com.example.music.data.CruiseManager
import kotlinx.coroutines.flow.StateFlow

class CruiseViewModel : ViewModel() {
    val speedKmh: StateFlow<Float> = CruiseManager.speedKmh
    val gpsBearing: StateFlow<Float> = CruiseManager.gpsBearing
    val phoneBearing: StateFlow<Float> = CruiseManager.phoneBearing
    val latitude: StateFlow<Double> = CruiseManager.latitude
    val longitude: StateFlow<Double> = CruiseManager.longitude
}