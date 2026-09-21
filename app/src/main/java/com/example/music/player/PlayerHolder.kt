// ============================================================
// player/PlayerHolder.kt
// 全局播放控制器 + 通知栏封面/歌词 + 封面磁盘缓存
// ============================================================
package com.example.music.player

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.music.MusicApp
import com.example.music.data.LrcLine
import com.example.music.data.Song
import com.example.music.data.api.ApiHolder
import com.example.music.data.toMediaItem
import com.example.music.data.toSong
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

object PlayerHolder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var future: ListenableFuture<MediaController>? = null

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: MediaController? get() = _controller.value

    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val position = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    val playMode = MutableStateFlow(PlayMode.LIST_LOOP)
    val lyric = MutableStateFlow<List<LrcLine>>(emptyList())
    val coverUrl = MutableStateFlow<String?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    // ---- 通知栏封面 & 歌词相关 ----
    private var infoJob: Job? = null
    private var lyricJob: Job? = null
    private var cachedArtworkData: ByteArray? = null
    private var currentLyricList: List<LrcLine> = emptyList()
    private var lastLyricLine: String? = null

    // ---------------- 初始化 ----------------

    fun init(context: Context) {
        if (future != null) return
        val appCtx = context.applicationContext
        val token = SessionToken(appCtx, ComponentName(appCtx, PlaybackService::class.java))
        val f = MediaController.Builder(appCtx, token).buildAsync()
        future = f

        f.addListener({
            runCatching {
                val c = f.get()
                _controller.value = c
                c.addListener(playerListener)
                playMode.value = PlayMode.of(c)
                currentSong.value = c.currentMediaItem?.toSong()
                isPlaying.value = c.isPlaying
                duration.value = c.duration.coerceAtLeast(0)
            }.onFailure { Log.e("PlayerHolder", "MediaController 连接失败", it) }
        }, MoreExecutors.directExecutor())

        scope.launch {
            while (isActive) {
                controller?.let { c ->
                    position.value = c.currentPosition.coerceAtLeast(0)
                    val d = c.duration
                    duration.value = if (d > 0) d else 0L
                }
                delay(500)
            }
        }
    }

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying.value = playing
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val s = mediaItem?.toSong()
            val prev = currentSong.value

            // 同一首歌的元数据更新（replaceMediaItem），只刷新 song 对象
            if (s != null && prev != null
                && s.id == prev.id && s.source == prev.source) {
                currentSong.value = s
                return
            }

            // 真正的切歌：重置所有状态
            currentSong.value = s
            lyric.value = emptyList()
            coverUrl.value = null
            cachedArtworkData = null
            currentLyricList = emptyList()
            lastLyricLine = null

            if (s != null) loadSongInfo(s)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            playMode.value = PlayMode.of(controller)
        }

        override fun onShuffleModeEnabledChanged(enabled: Boolean) {
            playMode.value = PlayMode.of(controller)
        }

        override fun onPlayerError(error: PlaybackException) {
            errorMessage.value = "播放失败：${error.errorCodeName}"
            Log.e("PlayerHolder", "播放错误", error)
        }
    }

    // ---------------- 歌曲信息加载 ----------------

    private fun loadSongInfo(song: Song) {
        infoJob?.cancel()
        infoJob = scope.launch {
            val info = runCatching { ApiHolder.api.fetchSongInfo(song) }.getOrNull() ?: return@launch
            if (currentSong.value?.id != song.id) return@launch

            coverUrl.value = info.cover

            val parsed = LrcParser.parse(info.lyric)
            currentLyricList = parsed
            lyric.value = parsed

            val artwork = info.cover?.let { url -> downloadArtwork(url) }
            cachedArtworkData = artwork
            if (currentSong.value?.id != song.id) return@launch

            replaceCurrentMediaItem(song, artwork, null)
            startLyricUpdateLoop()
        }
    }

    private fun startLyricUpdateLoop() {
        lyricJob?.cancel()
        lyricJob = scope.launch {
            while (isActive) {
                val s = currentSong.value ?: break
                val pos = controller?.currentPosition ?: 0L
                val line = currentLyricList.lastOrNull { it.time <= pos + 200 }?.text
                if (!line.isNullOrBlank() && line != lastLyricLine) {
                    lastLyricLine = line
                    replaceCurrentMediaItem(s, cachedArtworkData, line)
                }
                delay(400)
            }
        }
    }

    private fun replaceCurrentMediaItem(song: Song, artwork: ByteArray?, lyricLine: String?) {
        val c = controller ?: return
        val index = c.currentMediaItemIndex
        if (index < 0) return
        val newItem = song.toMediaItem(artwork, lyricLine)
        runCatching {
            c.replaceMediaItem(index, newItem)
        }.onFailure {
            Log.w("PlayerHolder", "replaceMediaItem failed", it)
        }
    }

    // ---------------- 封面下载 + 磁盘缓存 ----------------

    private suspend fun downloadArtwork(url: String): ByteArray? = withContext(Dispatchers.IO) {
        // 1. 尝试读磁盘缓存
        val cacheFile = artworkCacheFile(url)
        if (cacheFile.exists() && cacheFile.length() > 0) {
            runCatching { return@withContext cacheFile.readBytes() }
        }

        // 2. 网络下载
        val bytes = runCatching {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", ApiHolder.UA)
                .apply {
                    when {
                        url.contains("gtimg.cn") || url.contains("qq.com") ->
                            header("Referer", "https://y.qq.com/")
                        url.contains("126.net") || url.contains("163.com") ->
                            header("Referer", "https://music.163.com/")
                    }
                }
                .build()

            ApiHolder.client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val raw = resp.body?.bytes() ?: return@withContext null
                compressArtwork(raw)
            }
        }.getOrNull()

        // 3. 写入磁盘缓存
        if (bytes != null && bytes.isNotEmpty()) {
            runCatching { cacheFile.writeBytes(bytes) }
        }
        bytes
    }

    private fun artworkCacheFile(url: String): File {
        val dir = File(MusicApp.instance.filesDir, "artwork_cache")
        if (!dir.exists()) dir.mkdirs()
        val name = MessageDigest.getInstance("MD5")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) } + ".img"
        return File(dir, name)
    }

    private fun compressArtwork(bytes: ByteArray): ByteArray {
        return try {
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
            val maxSize = 512
            val w = bmp.width
            val h = bmp.height
            val scale = minOf(maxSize.toFloat() / w, maxSize.toFloat() / h, 1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(bmp, (w * scale).toInt(), (h * scale).toInt(), true)
            } else bmp
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        } catch (e: Exception) {
            bytes
        }
    }

    // ---------------- 播放控制 ----------------

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        val c = controller ?: return
        if (songs.isEmpty()) return
        val items = songs.map { it.toMediaItem() }
        c.setMediaItems(items, startIndex.coerceIn(0, items.size - 1), 0L)
        c.prepare()
        c.play()
    }

    fun playShuffle(songs: List<Song>) {
        if (songs.isEmpty()) return
        playQueue(songs.shuffled(), 0)
        setPlayMode(PlayMode.SHUFFLE)
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun prev() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms.coerceAtLeast(0))
    }

    fun stop() {
        infoJob?.cancel()
        lyricJob?.cancel()
        controller?.stop()
        controller?.clearMediaItems()
        currentSong.value = null
    }

    fun setPlayMode(mode: PlayMode) {
        val c = controller ?: return
        when (mode) {
            PlayMode.LIST_LOOP -> {
                c.repeatMode = Player.REPEAT_MODE_ALL
                c.shuffleModeEnabled = false
            }
            PlayMode.SINGLE_LOOP -> {
                c.repeatMode = Player.REPEAT_MODE_ONE
                c.shuffleModeEnabled = false
            }
            PlayMode.SHUFFLE -> {
                c.repeatMode = Player.REPEAT_MODE_ALL
                c.shuffleModeEnabled = true
            }
            PlayMode.SEQUENTIAL -> {
                c.repeatMode = Player.REPEAT_MODE_OFF
                c.shuffleModeEnabled = false
            }
        }
        playMode.value = mode
    }

    fun cyclePlayMode() = setPlayMode(playMode.value.next())

    fun currentQueue(): List<Song> {
        val c = controller ?: return emptyList()
        return (0 until c.mediaItemCount).mapNotNull { c.getMediaItemAt(it).toSong() }
    }
}