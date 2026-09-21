package com.example.music.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Song(
    val id: String,
    val source: String,
    val name: String,
    val artist: String,
    val album: String,
    val sign: String,
)

/**
 * 转成 MediaItem。
 * @param artworkData 封面图片字节（用于通知栏显示封面）
 * @param lyricLine   当前歌词行（为空时通知栏显示歌手）
 */
fun Song.toMediaItem(
    artworkData: ByteArray? = null,
    lyricLine: String? = null,
): MediaItem {
    val uri = Uri.Builder()
        .scheme("music")
        .authority("play")
        .appendQueryParameter("source", source)
        .appendQueryParameter("id", id)
        .appendQueryParameter("sign", sign)
        .build()

    // 通知栏第二行：有歌词显示歌词，没歌词显示歌手
    val secondLine = if (!lyricLine.isNullOrBlank()) lyricLine else artist

    val metaBuilder = MediaMetadata.Builder()
        .setTitle(name)
        .setArtist(secondLine)        // 通知栏第二行
        .setAlbumArtist(artist)       // 保留原始歌手，供反解析使用
        .setAlbumTitle(album)
        .setIsBrowsable(false)
        .setIsPlayable(true)

    if (artworkData != null) {
        metaBuilder.setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
    }

    return MediaItem.Builder()
        .setMediaId("$source:$id")
        .setUri(uri)
        .setMediaMetadata(metaBuilder.build())
        .build()
}

fun MediaItem.toSong(): Song? {
    val md = mediaMetadata
    val title = md.title?.toString() ?: return null
    val parts = mediaId.split(":", limit = 2)
    if (parts.size != 2) return null
    val uri = localConfiguration?.uri ?: return null

    // 优先取 albumArtist（我们存原始歌手的地方），回退到 artist
    val originalArtist = md.albumArtist?.toString().orEmpty()
        .ifBlank { md.artist?.toString().orEmpty() }

    return Song(
        id = parts[1],
        source = parts[0],
        name = title,
        artist = originalArtist,
        album = md.albumTitle?.toString().orEmpty(),
        sign = uri.getQueryParameter("sign").orEmpty()
    )
}

data class SongInfo(val cover: String?, val lyric: String?)
data class LrcLine(val time: Long, val text: String)