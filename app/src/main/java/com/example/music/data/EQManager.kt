package com.example.music.data

import android.media.audiofx.Equalizer
import android.util.Log

/**
 * 全局 EQ 管理器。
 */
object EQManager {

    private const val TAG = "EQManager"

    private var equalizer: Equalizer? = null
    private var currentSessionId: Int = 0

    var bandCount: Int = 5
        private set

    var bandFreqs: IntArray = IntArray(0)
        private set

    var levelRange: ShortRange = ShortRange(-1500, 1500)
        private set

    private var isEnabled: Boolean = false

    var activePresetId: String = "flat"

    // ============================================================
    // 生命周期
    // ============================================================

    @Synchronized
    fun attach(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (currentSessionId == audioSessionId && equalizer != null) return

        detach()
        currentSessionId = audioSessionId
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            readDeviceCapabilities()
            val preset = findPreset(activePresetId)
            if (preset != null) {
                applyPresetInternal(preset)
            }
            Log.d(TAG, "EQ 已绑定 session=$audioSessionId, bands=$bandCount")
        } catch (e: Exception) {
            Log.w(TAG, "EQ 绑定失败：${e.message}")
            equalizer = null
        }
    }

    @Synchronized
    fun detach() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
        currentSessionId = 0
    }

    private fun readDeviceCapabilities() {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            levelRange = ShortRange(range[0], range[1])

            val numBands = eq.numberOfBands.toInt()
            bandCount = numBands
            bandFreqs = IntArray(numBands) { i ->
                try {
                    eq.getCenterFreq(i.toShort()).toInt() / 1000
                } catch (e: Exception) {
                    0
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "读取 EQ 能力失败：${e.message}")
        }
    }

    // ============================================================
    // 开关
    // ============================================================

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w(TAG, "设置 EQ 开关失败：${e.message}")
        }
    }

    fun isEnabled(): Boolean = isEnabled

    // ============================================================
    // 应用预设
    // ============================================================

    fun applyPreset(preset: EQPreset) {
        activePresetId = preset.id
        applyPresetInternal(preset)
    }

    private fun applyPresetInternal(preset: EQPreset) {
        val eq = equalizer ?: return
        try {
            val deviceBands = bandCount
            for (i in 0 until deviceBands) {
                val level = interpolateLevel(preset.bands, i, deviceBands)
                    .coerceIn(levelRange.start.toInt(), levelRange.endInclusive.toInt())
                eq.setBandLevel(i.toShort(), level.toShort())
            }
        } catch (e: Exception) {
            Log.w(TAG, "应用预设失败：${e.message}")
        }
    }

    private fun interpolateLevel(source: List<Int>, index: Int, targetCount: Int): Int {
        if (source.isEmpty()) return 0
        if (source.size == targetCount) return source[index]
        if (source.size == 1) return source[0]

        val srcPos = index.toFloat() / (targetCount - 1) * (source.size - 1)
        val lo = srcPos.toInt().coerceIn(0, source.size - 1)
        val hi = (lo + 1).coerceAtMost(source.size - 1)
        val frac = srcPos - lo
        return (source[lo] * (1 - frac) + source[hi] * frac).toInt()
    }

    // ============================================================
    // 手动调整
    // ============================================================

    fun setBandLevel(bandIndex: Int, levelMb: Int) {
        val eq = equalizer ?: return
        try {
            val clamped = levelMb.coerceIn(
                levelRange.start.toInt(), levelRange.endInclusive.toInt()
            )
            eq.setBandLevel(bandIndex.toShort(), clamped.toShort())
        } catch (e: Exception) {
            Log.w(TAG, "设置频段失败：${e.message}")
        }
    }

    fun readCurrentBandLevels(): List<Int> {
        val eq = equalizer ?: return emptyList()
        return try {
            (0 until bandCount).map { i ->
                eq.getBandLevel(i.toShort()).toInt()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ============================================================
    // 工具
    // ============================================================

    fun findPreset(id: String): EQPreset? {
        return EQPresets.byId(id)
    }

    fun bandLabel(index: Int): String {
        if (index >= bandFreqs.size) return "${index + 1}"
        val f = bandFreqs[index]
        return when {
            f >= 1000 -> "${f / 1000}kHz"
            else -> "${f}Hz"
        }
    }
}

data class ShortRange(val start: Short, val endInclusive: Short)