package com.example.music.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle

@Composable
fun AmapMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var cameraInitialized by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                onCreate(null)
                val aMap = map

                aMap.uiSettings.apply {
                    isZoomControlsEnabled = false
                    isCompassEnabled = false
                    isMyLocationButtonEnabled = false
                    isScaleControlsEnabled = false
                    isTiltGesturesEnabled = false
                    isRotateGesturesEnabled = false
                    isScrollGesturesEnabled = false
                    isZoomGesturesEnabled = false
                }

                // 显示"我"的位置 + 车头朝上，地图跟随转动
                aMap.isMyLocationEnabled = true
                val style = MyLocationStyle().apply {
                    myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)
                    showMyLocation(true)
                    interval(1000L)
                    // ★ 修复：用 android.graphics.Color.TRANSPARENT（Int 类型）
                    strokeColor(android.graphics.Color.TRANSPARENT)
                    radiusFillColor(android.graphics.Color.TRANSPARENT)
                }
                aMap.myLocationStyle = style
            }
        },
        update = { view ->
            if (!cameraInitialized && latitude != 0.0 && longitude != 0.0) {
                cameraInitialized = true
                val pos = LatLng(latitude, longitude)
                view.map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(pos, 17f)
                )
            }
        },
        modifier = modifier
    )
}