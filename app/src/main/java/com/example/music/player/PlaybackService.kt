// ============================================================
// player/PlaybackService.kt
// MediaSessionService：后台播放 + 耳机线控 + 通知栏控制 + EQ
// ============================================================
package com.example.music.player

import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.music.data.EQManager
import com.example.music.data.EQPresets
import com.example.music.data.UserPreferencesStore
import com.example.music.data.api.ApiHolder
import com.example.music.data.cache.AudioCache
import java.io.File
import java.io.IOException

class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        ApiHolder.infoCacheDir = File(filesDir, "song_info_cache")

        val cache = AudioCache.get(applicationContext)
        val prefs = UserPreferencesStore(applicationContext)

        // 1) 底层 HTTP
        val httpFactory = OkHttpDataSource.Factory(ApiHolder.client)

        // 2) 动态解析 music:// → 真实音频 URL
        val resolver = ResolvingDataSource.Resolver { dataSpec ->
            val uri = dataSpec.uri
            if (uri.scheme == "music") {
                val source = uri.getQueryParameter("source").orEmpty()
                val id = uri.getQueryParameter("id").orEmpty()
                val sign = uri.getQueryParameter("sign").orEmpty()
                val real = ApiHolder.api.getPlayUrlSync(id, source, sign)
                    ?: throw IOException("无法获取播放地址，可能受版权限制")
                dataSpec.withUri(Uri.parse(real))
            } else dataSpec
        }
        val resolvingFactory = ResolvingDataSource.Factory(httpFactory, resolver)

        // 3) 磁盘缓存
        val cacheFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(resolvingFactory)
            .setCacheKeyFactory { dataSpec ->
                val u = dataSpec.uri
                if (u.scheme == "music")
                    "${u.getQueryParameter("source")}:${u.getQueryParameter("id")}"
                else u.toString()
            }
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        // ★ ExoPlayer 构建完成后才能拿到 audioSessionId
        try {
            val audioSessionId = player!!.audioSessionId
            EQManager.activePresetId = prefs.getActiveEQId()
            EQManager.attach(audioSessionId)
            EQManager.setEnabled(prefs.isEQEnabled())

            val activeId = prefs.getActiveEQId()
            val preset = EQPresets.byId(activeId)
                ?: prefs.getCustomEQs().firstOrNull { it.id == activeId }
            if (preset != null) {
                EQManager.applyPreset(preset)
            }
        } catch (e: Exception) {
            // EQ 失败不影响播放
        }

        session = MediaSession.Builder(this, player!!).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = player ?: return
        if (!p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        try {
            EQManager.detach()
        } catch (_: Exception) {}
        session?.run {
            player.release()
            release()
        }
        session = null
        player = null
        super.onDestroy()
    }
}