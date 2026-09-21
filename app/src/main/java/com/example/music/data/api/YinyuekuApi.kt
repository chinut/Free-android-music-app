// ============================================================
// data/api/YinyuekuApi.kt
// 直连上游音乐接口（yinyueku.cn）+ 歌词/封面磁盘缓存
// ============================================================
package com.example.music.data.api

import android.util.Base64
import com.example.music.data.Song
import com.example.music.data.SongInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiHolder {
    const val UA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    /** 歌曲信息（封面 + 歌词）的磁盘缓存目录，由 PlaybackService 启动时设置 */
    var infoCacheDir: File? = null

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val req = chain.request()
                val host = req.url.host
                val b = req.newBuilder().header("User-Agent", UA)
                when {
                    host.endsWith("qq.com") || host.endsWith("gtimg.cn") ->
                        b.header("Referer", "https://y.qq.com/")
                    host.endsWith("126.net") || host.endsWith("163.com") ->
                        b.header("Referer", "https://music.163.com/")
                            .header("Cookie", "os=pc; appver=2.0.2")
                    host.endsWith("yinyueku.cn") ->
                        b.header("Referer", "http://www.yinyueku.cn/")
                            .header("Origin", "http://www.yinyueku.cn")
                }
                chain.proceed(b.build())
            }
            .build()
    }

    val api: YinyuekuApi by lazy { YinyuekuApi(client) }
}

class YinyuekuApi(private val client: OkHttpClient) {

    companion object {
        private const val ENDPOINT = "http://www.yinyueku.cn/api.php"
    }

    // ---------------- 底层请求 ----------------

    private fun buildFormRequest(params: Map<String, String>): Request {
        val form = FormBody.Builder()
        params.forEach { (k, v) -> form.add(k, v) }
        return Request.Builder()
            .url(ENDPOINT)
            .post(form.build())
            .header("Referer", "http://www.yinyueku.cn/")
            .header("Origin", "http://www.yinyueku.cn")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .build()
    }

    private fun postSync(params: Map<String, String>): String {
        client.newCall(buildFormRequest(params)).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("上游 HTTP ${resp.code}")
            return resp.body?.string() ?: throw IOException("空响应")
        }
    }

    private fun getText(url: String, referer: String?): String {
        val b = Request.Builder().url(url)
        if (referer != null) b.header("Referer", referer)
        client.newCall(b.build()).execute().use { r ->
            if (!r.isSuccessful) throw IOException("HTTP ${r.code}")
            return r.body?.string() ?: throw IOException("空响应")
        }
    }

    // ---------------- 工具：解码 HTML 实体（不丢失换行） ----------------

    private fun decodeHtmlEntities(s: String): String {
        if (s.isEmpty() || !s.contains('&')) return s
        return s
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("&#x([0-9a-fA-F]+);")) { m ->
                m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: m.value
            }
            .replace(Regex("&#(\\d+);")) { m ->
                m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: m.value
            }
    }

    private fun normalizeLyric(raw: String): String {
        var s = raw
        if (!s.startsWith("[") && s.length > 64) {
            runCatching {
                val dec = String(Base64.decode(s, Base64.DEFAULT), Charsets.UTF_8)
                if (dec.contains("[")) s = dec
            }
        }
        s = decodeHtmlEntities(s)
        s = s.replace("\\n", "\n")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
        return s
    }

    // ---------------- 解析歌曲 ----------------

    private fun optArtist(o: JSONObject): String {
        val v = o.opt("artist") ?: o.opt("singer") ?: o.opt("song_artist") ?: o.opt("artists")
        return when (v) {
            is JSONArray -> (0 until v.length()).mapNotNull { i ->
                when (val e = v.opt(i)) {
                    is String -> e
                    is JSONObject -> e.optString("name").ifEmpty { e.optString("title") }
                    else -> null
                }
            }.filter { it.isNotBlank() }.joinToString(" / ")
            is String -> v
            else -> ""
        }
    }

    private fun optAlbum(o: JSONObject): String {
        val v = o.opt("album") ?: o.opt("song_album") ?: o.opt("al")
        return when (v) {
            is String -> v
            is JSONObject -> v.optString("name").ifEmpty { v.optString("title") }
            else -> ""
        }
    }

    private fun parseSong(o: JSONObject): Song? {
        val id = (o.optString("id").ifEmpty { null }
            ?: o.optString("url_id").ifEmpty { null }
            ?: o.optString("song_id").ifEmpty { null }
            ?: o.optString("songmid").ifEmpty { null }) ?: return null
        val source = (o.optString("source").ifEmpty { null }
            ?: o.optString("song_source").ifEmpty { null }) ?: return null
        val sign = (o.optString("sign").ifEmpty { null }
            ?: o.optString("song_sign").ifEmpty { null }) ?: return null
        val name = o.optString("name").ifEmpty { o.optString("song_name") }
            .ifEmpty { o.optString("title") }
        if (name.isBlank()) return null
        return Song(
            id = id, source = source, name = name,
            artist = optArtist(o), album = optAlbum(o), sign = sign
        )
    }

    private fun parseSongList(text: String): List<Song> {
        val trimmed = text.trim()
        val arr: JSONArray = try {
            when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                else -> {
                    val obj = JSONObject(trimmed)
                    val d = obj.opt("data") ?: obj.opt("list") ?: obj.opt("songs")
                    when (d) {
                        is JSONArray -> d
                        is JSONObject -> d.optJSONArray("list")
                            ?: d.optJSONArray("data") ?: JSONArray()
                        else -> JSONArray()
                    }
                }
            }
        } catch (e: Exception) { JSONArray() }

        val out = ArrayList<Song>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            parseSong(o)?.let { out.add(it) }
        }
        return out
    }

    // ---------------- 对外 API ----------------

    suspend fun search(keyword: String, page: Int = 1): List<Song> = withContext(Dispatchers.IO) {
        val text = postSync(
            mapOf(
                "types" to "search",
                "count" to "20",
                "pages" to page.toString(),
                "name" to keyword
            )
        )
        parseSongList(text)
    }

    suspend fun searchArtist(artist: String, count: Int = 15): List<Song> =
        withContext(Dispatchers.IO) {
            val text = postSync(
                mapOf("types" to "search", "count" to count.toString(),
                    "pages" to "1", "name" to artist)
            )
            parseSongList(text)
        }

    suspend fun getPlayUrl(
        id: String, source: String, sign: String, br: String = "320kmp3"
    ): String? = withContext(Dispatchers.IO) {
        val text = postSync(
            mapOf("types" to "url", "id" to id, "source" to source, "br" to br, "sign" to sign)
        )
        val o = JSONObject(text)
        o.optString("url").ifEmpty {
            o.optString("data").ifEmpty { null }
        }
    }

    fun getPlayUrlSync(id: String, source: String, sign: String, br: String = "320kmp3"): String? {
        return try {
            val text = postSync(
                mapOf("types" to "url", "id" to id, "source" to source, "br" to br, "sign" to sign)
            )
            val o = JSONObject(text)
            o.optString("url").ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- 歌曲信息（封面 + 歌词） ----------------
    // 说明：磁盘缓存优先，未命中才请求接口

    suspend fun fetchSongInfo(song: Song): SongInfo = withContext(Dispatchers.IO) {
        // 1. 尝试读磁盘缓存
        val cacheFile = cacheFileFor(song)
        if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0) {
            runCatching {
                val o = JSONObject(cacheFile.readText())
                val cover = o.optString("cover").ifEmpty { null }
                val lyric = o.optString("lyric").ifEmpty { null }
                if (cover != null || lyric != null) {
                    return@withContext SongInfo(cover, lyric)
                }
            }
        }

        // 2. 缓存未命中：请求接口
        val info = runCatching {
            when (song.source) {
                "netease" -> fetchNeteaseInfo(song.id)
                "tencent" -> fetchTencentInfo(song.id)
                else -> SongInfo(null, null)
            }
        }.getOrElse { SongInfo(null, null) }

        // 3. 写入磁盘缓存
        if ((info.cover != null || info.lyric != null) && cacheFile != null) {
            runCatching {
                val o = JSONObject().apply {
                    put("cover", info.cover ?: "")
                    put("lyric", info.lyric ?: "")
                    put("cached_at", System.currentTimeMillis())
                }
                cacheFile.writeText(o.toString())
            }
        }

        info
    }

    private fun cacheFileFor(song: Song): File? {
        val dir = ApiHolder.infoCacheDir ?: return null
        if (!dir.exists()) dir.mkdirs()
        val safeId = song.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return File(dir, "${song.source}_$safeId.json")
    }

    // -------- 网易云 --------

    private fun fetchNeteaseInfo(id: String): SongInfo {
        var cover: String? = null
        var lyric: String? = null

        // 封面：先试新版 API v3
        runCatching {
            val body = getText(
                "https://music.163.com/api/v3/song/detail?id=$id" +
                        "&c=%5B%7B%22id%22%3A%22$id%22%7D%5D",
                "https://music.163.com/"
            )
            val o = JSONObject(body)
            val s0 = o.optJSONArray("songs")?.optJSONObject(0)
            val al = s0?.optJSONObject("al") ?: s0?.optJSONObject("album")
            cover = al?.optString("picUrl")?.ifEmpty { null }
        }

        // 回退旧版
        if (cover == null) {
            runCatching {
                val body = getText(
                    "https://music.163.com/api/song/detail/?id=$id&ids=%5B$id%5D",
                    "https://music.163.com/"
                )
                val o = JSONObject(body)
                val s0 = o.optJSONArray("songs")?.optJSONObject(0)
                val al = s0?.optJSONObject("album") ?: s0?.optJSONObject("al")
                cover = al?.optString("picUrl")?.ifEmpty { null }
            }
        }

        // 歌词
        runCatching {
            val body = getText(
                "https://music.163.com/api/song/lyric?id=$id&lv=1&kv=1&tv=-1",
                "https://music.163.com/"
            )
            val o = JSONObject(body)
            val raw = o.optJSONObject("lrc")?.optString("lyric")?.ifEmpty { null }
            if (raw != null) {
                lyric = normalizeLyric(raw)
            }
        }
        return SongInfo(cover, lyric)
    }

    // -------- QQ 音乐 --------

    private fun fetchTencentInfo(id: String): SongInfo {
        var cover: String? = null
        var lyric: String? = null

        // 封面
        runCatching {
            val body = getText(
                "https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg" +
                        "?songmid=$id&platform=yqq&format=json",
                "https://y.qq.com/"
            )
            val o = JSONObject(body)
            val albumMid = o.optJSONArray("data")?.optJSONObject(0)
                ?.optJSONObject("album")?.optString("mid").orEmpty()
            if (albumMid.isNotEmpty()) {
                cover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000$albumMid.jpg"
            }
        }

        // 歌词
        runCatching {
            val body = getText(
                "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg" +
                        "?songmid=$id&format=json&nobase64=1&g_tk=5381",
                "https://y.qq.com/portal/player.html"
            )
            val o = JSONObject(body)
            val raw = o.optString("lyric").ifEmpty { null }
            if (raw != null) {
                lyric = normalizeLyric(raw)
            }
        }
        return SongInfo(cover, lyric)
    }
}