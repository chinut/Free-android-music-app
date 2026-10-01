package com.example.music.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.music.data.tv.TvStreamUrl

/**
 * 直连流（m3u8 / flv）的原生播放器。
 *
 * 为什么不用网页里的 hls.js：
 * - 浏览器禁止 JS 设置 Referer，而国内大量直播源缺 Referer 直接 403；
 * - 部分源是无扩展名的 m3u8 接口，hls.js 认不出来。
 * 原生 ExoPlayer 能自定义请求头，稳定得多。
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun NativeStreamPlayer(
    streamUrl: String,
    modifier: Modifier = Modifier,
    /** 变化时重建播放器（用于「重载」）。 */
    retryKey: Int = 0,
    onReady: () -> Unit = {},
    onError: (String) -> Unit = {},
    playerRef: (ExoPlayer?) -> Unit = {},
) {
    val context = LocalContext.current

    val player = remember(streamUrl, retryKey) {
        val headers = TvStreamUrl.headersFor(streamUrl)

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(headers["User-Agent"] ?: "okhttp")
            .setDefaultRequestProperties(headers)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setAllowCrossProtocolRedirects(true)

        // 直播场景：加大缓冲，减少卡顿
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,   // 最小缓冲
                60_000,   // 最大缓冲
                2_000,    // 起播缓冲
                4_000,    // 重缓冲
            )
            .build()

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(if (isHls(streamUrl)) {
                HlsMediaSource.Factory(dataSourceFactory)
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
            })
            .setLoadControl(loadControl)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(streamUrl, retryKey) {
        playerRef(player)
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) onReady()
            }

            override fun onPlayerError(error: PlaybackException) {
                onError(error.errorCodeName)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
            playerRef(null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(android.graphics.Color.BLACK)
                addView(
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        setBackgroundColor(android.graphics.Color.BLACK)
                        // 手机端直接上手势/触摸控制
                        setControllerAutoShow(true)
                        setControllerShowTimeoutMs(4000)
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
        },
    )
}

private fun isHls(url: String): Boolean {
    val u = url.lowercase()
    return u.contains(".m3u8") ||
        u.contains("m3u8") ||
        u.contains("application/vnd.apple.mpegurl") ||
        u.contains(MimeTypes.APPLICATION_M3U8.lowercase())
}
