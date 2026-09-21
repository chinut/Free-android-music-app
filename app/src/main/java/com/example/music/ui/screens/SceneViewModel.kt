package com.example.music.ui.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.data.RecommendEngine
import com.example.music.data.SceneMode
import com.example.music.data.Song
import com.example.music.data.UserPreferencesStore
import com.example.music.data.api.ApiHolder
import com.example.music.player.PlayerHolder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SceneViewModel(app: Application) : AndroidViewModel(app) {

    var loading by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set
    var lastScene by mutableStateOf<SceneMode?>(null)
        private set

    private val api = ApiHolder.api
    private val prefs = UserPreferencesStore(app)

    /**
     * 播放某个情景。
     * 优先从该场景的歌手池里挑歌，同时过滤掉黑名单。
     */
    fun playScene(scene: SceneMode, onResult: (String) -> Unit = {}) {
        if (loading) return
        viewModelScope.launch {
            loading = true
            lastScene = scene
            message = "正在准备「${scene.label}」…"
            try {
                val blockedArtists = prefs.getBlacklistArtists()
                val blockedGenres = prefs.getBlacklistGenres()
                val blockedKeywords = prefs.getBlacklistKeywords()

                // 过滤掉黑名单里的歌手
                val sceneArtists = scene.artists.filter { a ->
                    !RecommendEngine.isArtistBlocked(
                        a, blockedArtists, blockedGenres, blockedKeywords
                    )
                }.ifEmpty {
                    // 场景歌手全被屏蔽时，退回到所有歌手
                    RecommendEngine.ALL_ARTISTS.filter { a ->
                        !RecommendEngine.isArtistBlocked(
                            a, blockedArtists, blockedGenres, blockedKeywords
                        )
                    }
                }

                // 从场景歌手池里随机挑 4 位
                val picked = sceneArtists.shuffled().take(4)
                if (picked.isEmpty()) {
                    message = "没有可播放的歌手"
                    loading = false
                    return@launch
                }

                // 并发搜索
                val result = coroutineScope {
                    picked.map { artist ->
                        async {
                            runCatching { api.searchArtist(artist, count = 12) }
                                .getOrDefault(emptyList())
                        }
                    }.awaitAll().flatten()
                }

                // 去重 + 打乱 + 交错
                val seen = HashSet<String>()
                val unique = result.filter { seen.add("${it.source}:${it.id}") }
                val interleaved = RecommendEngine.interleave(unique.shuffled())
                val songs: List<Song> = interleaved.take(40)

                if (songs.isEmpty()) {
                    message = "没有找到相关歌曲"
                    loading = false
                    return@launch
                }

                // 播放
                PlayerHolder.playQueue(songs, 0)
                message = "${scene.emoji} ${scene.label} · ${songs.size} 首"
                onResult(message)
            } catch (e: Exception) {
                message = "失败：${e.message}"
                onResult(message)
            } finally {
                loading = false
            }
        }
    }
}