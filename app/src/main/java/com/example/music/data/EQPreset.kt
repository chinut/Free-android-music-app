package com.example.music.data

/**
 * EQ 预设。
 * bands 单位：毫贝（mB），100 mB = 1 dB，范围 -1500 ~ 1500。
 * 基准是 5 段 EQ。设备支持的段数不同时，会自动做频率近似映射。
 */
data class EQPreset(
    val id: String,
    val name: String,
    val bands: List<Int>,
    val isBuiltIn: Boolean = false,
)

object EQPresets {
    val flat = EQPreset("flat", "平直", listOf(0, 0, 0, 0, 0), true)
    val bass = EQPreset("bass", "低音增强", listOf(650, 450, 150, -100, -200), true)
    val vocal = EQPreset("vocal", "人声增强", listOf(-250, 0, 500, 450, 100), true)
    val pop = EQPreset("pop", "流行", listOf(-100, 200, 450, 200, -100), true)
    val rock = EQPreset("rock", "摇滚", listOf(500, 250, -100, 350, 500), true)
    val jazz = EQPreset("jazz", "爵士", listOf(350, 150, 200, 100, 350), true)
    val classical = EQPreset("classical", "古典", listOf(450, 200, -100, 200, 450), true)
    val dance = EQPreset("dance", "舞曲", listOf(650, 350, 0, 300, 500), true)

    val ALL: List<EQPreset> = listOf(
        flat, bass, vocal, pop, rock, jazz, classical, dance
    )

    fun byId(id: String): EQPreset? = ALL.firstOrNull { it.id == id }
}