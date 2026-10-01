package com.example.music.data

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

object CruiseManager {

    private var locationClient: AMapLocationClient? = null
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    // ★ 用于读取当前屏幕旋转
    private var windowManager: WindowManager? = null

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh

    private val _gpsBearing = MutableStateFlow(0f)
    val gpsBearing: StateFlow<Float> = _gpsBearing

    /** 手机朝向（已低通滤波 + 节流后的值） */
    private val _phoneBearing = MutableStateFlow(0f)
    val phoneBearing: StateFlow<Float> = _phoneBearing

    private val _latitude = MutableStateFlow(0.0)
    val latitude: StateFlow<Double> = _latitude

    private val _longitude = MutableStateFlow(0.0)
    val longitude: StateFlow<Double> = _longitude

    // ============================================================
    // 手机方向传感器：低通滤波 + 节流
    // ============================================================

    private var filteredBearing: Float = 0f
    private var lastEmittedBearing: Float = 0f
    private var lastEmitTime: Long = 0L

    private val smoothingFactor = 0.15f
    private val minEmitIntervalMs = 80L
    private val minEmitDelta = 1.5f

    private val sensorListener = object : SensorEventListener {
        private val rotationMatrix = FloatArray(9)
        private val orientation = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            try {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // 传感器原始角度（弧度 → 度）
                var rawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()

                // ★ 关键修复：按屏幕旋转方向修正
                // 让"屏幕上方"作为朝向参考，而不是永远用听筒方向
                rawDeg = (rawDeg + screenRotationOffset() + 360f) % 360f

                // ---- 1) 低通滤波 ----
                var delta = rawDeg - filteredBearing
                if (delta > 180f) delta -= 360f
                if (delta < -180f) delta += 360f
                filteredBearing = (filteredBearing + delta * smoothingFactor + 360f) % 360f

                // ---- 2) 节流 ----
                val now = System.currentTimeMillis()
                if (now - lastEmitTime < minEmitIntervalMs) return

                var emitDelta = filteredBearing - lastEmittedBearing
                if (emitDelta > 180f) emitDelta -= 360f
                if (emitDelta < -180f) emitDelta += 360f
                if (abs(emitDelta) < minEmitDelta) return

                lastEmitTime = now
                lastEmittedBearing = filteredBearing
                _phoneBearing.value = filteredBearing
            } catch (_: Exception) {}
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * 根据屏幕旋转方向，返回需要叠加到方位角上的偏移量（度）。
     *
     * 逻辑：
     *   - ROTATION_0   ：竖屏，屏幕上方 = 听筒方向，无需修正
     *   - ROTATION_90  ：手机顺时针转90°（顶部朝左），屏幕上方 = 手机右侧 → +90°
     *   - ROTATION_180 ：倒竖屏 → +180°
     *   - ROTATION_270 ：手机逆时针转90°（顶部朝右），屏幕上方 = 手机左侧 → -90°
     */
    private fun screenRotationOffset(): Float {
        val rotation = try {
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        } catch (_: Exception) {
            Surface.ROTATION_0
        }
        return when (rotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> -90f
            else -> 0f
        }
    }

    @SuppressLint("MissingPermission")
    fun start(context: Context) {
        // ★ 保存 windowManager，供传感器回调使用
        if (windowManager == null) {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        }

        // ---- 定位 ----
        if (locationClient == null) {
            try {
                AMapLocationClient.updatePrivacyShow(context, true, true)
                AMapLocationClient.updatePrivacyAgree(context, true)
                val client = AMapLocationClient(context.applicationContext)
                val option = AMapLocationClientOption().apply {
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    interval = 1000L
                    isNeedAddress = false
                    isMockEnable = false
                    isLocationCacheEnable = false
                }
                client.setLocationOption(option)
                client.setLocationListener { loc: AMapLocation? ->
                    if (loc == null) return@setLocationListener
                    if (loc.errorCode != 0) return@setLocationListener
                    val speed = (loc.speed * 3.6f).coerceIn(0f, 180f)
                    _speedKmh.value = speed
                    _gpsBearing.value = loc.bearing
                    _latitude.value = loc.latitude
                    _longitude.value = loc.longitude
                }
                client.startLocation()
                locationClient = client
            } catch (_: Exception) {}
        }

        // ---- 方向传感器 ----
        if (sensorManager == null) {
            try {
                val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                if (sensor != null) {
                    sm.registerListener(
                        sensorListener,
                        sensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                    sensorManager = sm
                    rotationSensor = sensor
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try { locationClient?.stopLocation() } catch (_: Exception) {}
        try { locationClient?.onDestroy() } catch (_: Exception) {}
        locationClient = null

        try {
            if (sensorManager != null && rotationSensor != null) {
                sensorManager?.unregisterListener(sensorListener, rotationSensor)
            }
        } catch (_: Exception) {}
        sensorManager = null
        rotationSensor = null

        _speedKmh.value = 0f
        _gpsBearing.value = 0f
        _phoneBearing.value = 0f
        filteredBearing = 0f
        lastEmittedBearing = 0f
        lastEmitTime = 0L
    }
}