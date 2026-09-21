// ============================================================
// 9. player/PlayMode.kt
// ============================================================
package com.example.music.player

import androidx.media3.common.Player

enum class PlayMode(val label: String) {
    LIST_LOOP("列表循环"),
    SINGLE_LOOP("单曲循环"),
    SHUFFLE("随机播放"),
    SEQUENTIAL("顺序播放");

    fun next(): PlayMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun of(player: Player?): PlayMode {
            if (player == null) return LIST_LOOP
            return when {
                player.repeatMode == Player.REPEAT_MODE_ONE -> SINGLE_LOOP
                player.shuffleModeEnabled -> SHUFFLE
                player.repeatMode == Player.REPEAT_MODE_ALL -> LIST_LOOP
                else -> SEQUENTIAL
            }
        }
    }
}