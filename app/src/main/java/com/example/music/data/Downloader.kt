// ============================================================
// 8. data/Downloader.kt  —— 下载 MP3 到系统音乐库
// ============================================================
package com.example.music.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.music.data.api.ApiHolder
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.IOException

object Downloader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun sanitize(s: String) =
        s.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(120)

    fun download(context: Context, song: Song, onResult: (Boolean, String) -> Unit) {
        val appCtx = context.applicationContext
        scope.launch {
            try {
                val url = ApiHolder.api.getPlayUrl(song.id, song.source, song.sign)
                    ?: throw IOException("获取下载地址失败，可能受版权限制")

                val fileName = sanitize("${song.artist} - ${song.name}") + ".mp3"
                val resolver = appCtx.contentResolver

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_MUSIC + "/Yinyueku")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                val uri = resolver.insert(collection, values)
                    ?: throw IOException("无法创建媒体文件（可能缺少存储权限）")

                try {
                    val req = Request.Builder()
                        .url(url)
                        .header("User-Agent", ApiHolder.UA)
                        .header("Referer", "https://y.qq.com/")
                        .build()

                    ApiHolder.client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
                        val body = resp.body ?: throw IOException("空响应")
                        resolver.openOutputStream(uri)?.use { out ->
                            body.byteStream().copyTo(out)
                        } ?: throw IOException("无法写入文件")
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val done = ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        }
                        resolver.update(uri, done, null, null)
                    }
                    withContext(Dispatchers.Main) {
                        onResult(true, "已保存到「音乐/Yinyueku」")
                    }
                } catch (e: Exception) {
                    runCatching { resolver.delete(uri, null, null) }
                    throw e
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "下载失败")
                }
            }
        }
    }
}