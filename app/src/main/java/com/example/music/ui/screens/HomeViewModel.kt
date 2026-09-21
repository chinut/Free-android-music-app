package com.example.music.ui.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.MusicApp
import com.example.music.data.PlaylistRepository
import com.example.music.data.RecommendEngine
import com.example.music.data.Song
import com.example.music.data.UserPreferencesStore
import com.example.music.data.api.ApiHolder
import com.example.music.player.PlayerHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    var songs by mutableStateOf<List<Song>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var title by mutableStateOf("🎧 为你推荐")
        private set
    var subtitle by mutableStateOf("")
        private set
    var currentSong by mutableStateOf<Song?>(null)
        private set

    // ★ 已收藏歌曲的 key 集合（"source:songId"）
    var favoritedKeys by mutableStateOf<Set<String>>(emptySet())
        private set

    private val api = ApiHolder.api
    private val prefs = UserPreferencesStore(app)
    private val repo = PlaylistRepository(app)

    private var lastRecordedArtist: String? = null
    private var lastRecordedTime: Long = 0L

    init {
        // 监听播放
        viewModelScope.launch {
            PlayerHolder.currentSong.collectLatest { song ->
                currentSong = song
                song?.let {
                    val now = System.currentTimeMillis()
                    if (it.artist != lastRecordedArtist || now - lastRecordedTime > 30_000) {
                        lastRecordedArtist = it.artist
                        lastRecordedTime = now
                        prefs.recordPlay(it.artist)
                    }
                }
            }
        }

        // ★ 实时观察收藏集合
        viewModelScope.launch {
            repo.allFavoritedKeys().collect { keys ->
                favoritedKeys = keys
            }
        }
    }

    fun isFavorited(song: Song): Boolean =
        "${song.source}:${song.id}" in favoritedKeys

    fun loadHot(force: Boolean = false) {
        if (loading && !force) return
        viewModelScope.launch {
            loading = true
            title = "🎧 为你推荐"
            try {
                val picked = RecommendEngine.pickArtists(prefs, topN = 6)
                val safePicked = if (picked.size >= 3) picked
                else (picked + listOf("周杰伦", "林俊杰", "Taylor Swift"))
                    .distinct().take(3)

                val result = coroutineScope {
                    safePicked.map { artist ->
                        async {
                            runCatching { api.searchArtist(artist, count = 12) }
                                .getOrDefault(emptyList())
                        }
                    }.awaitAll().flatten()
                }

                val seen = HashSet<String>()
                val unique = result.filter { seen.add("${it.source}:${it.id}") }

                val shuffled = unique.shuffled()
                val interleaved = RecommendEngine.interleave(shuffled)

                songs = interleaved.take(60)
                subtitle = "${songs.size} 首 · ${safePicked.joinToString("、")}"

                maybeAutoPlay()
            } catch (e: Exception) {
                subtitle = "加载失败：${e.message}"
            } finally {
                loading = false
            }
        }
    }

    private fun maybeAutoPlay() {
        val app = getApplication<MusicApp>()
        if (app.autoPlayTriggered) return
        if (songs.isEmpty()) return

        if (!prefs.isAutoPlayEnabled()) {
            app.autoPlayTriggered = true
            return
        }

        if (PlayerHolder.currentSong.value != null) {
            app.autoPlayTriggered = true
            return
        }

        app.autoPlayTriggered = true

        viewModelScope.launch {
            delay(600)

            val candidates = songs.take(3)
            val oks = candidates.map { song ->
                async(Dispatchers.IO) {
                    runCatching {
                        val url = api.getPlayUrl(song.id, song.source, song.sign)
                        !url.isNullOrBlank()
                    }.getOrDefault(false)
                }
            }.awaitAll()

            val playIndex = oks.indexOfFirst { it }.let {
                if (it >= 0) it else 0
            }

            if (songs.isNotEmpty() && playIndex in songs.indices) {
                PlayerHolder.playQueue(songs, playIndex)
            }
        }
    }

    fun search(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) return

        prefs.recordSearch(kw)

        viewModelScope.launch {
            loading = true
            title = "🔍 搜索：$kw"
            try {
                val list = api.search(kw)
                val seen = HashSet<String>()
                songs = list.filter { seen.add("${it.source}:${it.id}") }
                subtitle = "${songs.size} 首"
            } catch (e: Exception) {
                songs = emptyList()
                subtitle = "搜索失败：${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun playAt(index: Int) {
        PlayerHolder.playQueue(songs, index)
    }

    fun playAll() {
        PlayerHolder.playQueue(songs, 0)
    }

    fun playShuffle() {
        PlayerHolder.playShuffle(songs)
    }
}