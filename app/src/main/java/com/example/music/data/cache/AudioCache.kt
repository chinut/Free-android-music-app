// ============================================================
// data/cache/AudioCache.kt
// 音频缓存 + 总缓存大小统计
// ============================================================
package com.example.music.data.cache

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object AudioCache {

    /** 缓存上限 500MB，超出自动淘汰最久未使用的音频 */
    private const val MAX_BYTES = 500L * 1024 * 1024

    @Volatile
    private var instance: SimpleCache? = null

    fun get(context: Context): SimpleCache {
        instance?.let { return it }
        return synchronized(this) {
            instance ?: run {
                val dir = File(context.filesDir, "audio_cache")
                if (!dir.exists()) dir.mkdirs()
                val evictor = LeastRecentlyUsedCacheEvictor(MAX_BYTES)
                val dbProvider = StandaloneDatabaseProvider(context.applicationContext)
                SimpleCache(dir, evictor, dbProvider).also { instance = it }
            }
        }
    }

    fun cacheDir(context: Context): File = File(context.filesDir, "audio_cache")

    /** 音频缓存的字节数 */
    fun sizeBytes(context: Context): Long {
        val dir = cacheDir(context)
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /** 音频 + 歌词 + 封面的总字节数 */
    fun totalCacheBytes(context: Context): Long {
        val audio = sizeBytes(context)
        val info = File(context.filesDir, "song_info_cache").let {
            if (it.exists()) it.walkTopDown().filter { f -> f.isFile }.sumOf { f -> f.length() } else 0
        }
        val art = File(context.filesDir, "artwork_cache").let {
            if (it.exists()) it.walkTopDown().filter { f -> f.isFile }.sumOf { f -> f.length() } else 0
        }
        return audio + info + art
    }

    /** 清空音频缓存（调用前建议先暂停播放） */
    fun clear(): Int {
        val c = instance ?: return 0
        var n = 0
        runCatching {
            c.keys.toList().forEach { key ->
                runCatching { c.removeResource(key) }.onSuccess { n++ }
            }
        }
        return n
    }
}