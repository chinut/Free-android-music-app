package com.example.music.data.share

import android.util.Base64
import com.example.music.data.Song
import org.json.JSONArray
import org.json.JSONObject

/**
 * 分享码编解码。
 * 格式：
 *   MUSIC_PLAYLIST:<base64(json)>   —— 歌单
 *   MUSIC_SONG:<base64(json)>       —— 单曲
 */
object ShareCodec {

    private const val PREFIX_PLAYLIST = "MUSIC_PLAYLIST:"
    private const val PREFIX_SONG = "MUSIC_SONG:"

    // ---------------- 编码 ----------------

    fun encodePlaylist(name: String, songs: List<Song>): String {
        val arr = JSONArray()
        songs.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("source", s.source)
                put("name", s.name)
                put("artist", s.artist)
                put("album", s.album)
                put("sign", s.sign)
            })
        }
        val root = JSONObject().apply {
            put("v", 1)
            put("name", name)
            put("songs", arr)
        }
        return PREFIX_PLAYLIST + Base64.encodeToString(
            root.toString().toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }

    fun encodeSong(song: Song): String {
        val root = JSONObject().apply {
            put("v", 1)
            put("id", song.id)
            put("source", song.source)
            put("name", song.name)
            put("artist", song.artist)
            put("album", song.album)
            put("sign", song.sign)
        }
        return PREFIX_SONG + Base64.encodeToString(
            root.toString().toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }

    // ---------------- 解码 ----------------

    sealed class Decoded {
        data class Playlist(val name: String, val songs: List<Song>) : Decoded()
        data class Single(val song: Song) : Decoded()
        data class Invalid(val reason: String) : Decoded()
    }

    fun decode(text: String?): Decoded {
        if (text.isNullOrBlank()) return Decoded.Invalid("内容为空")
        val trimmed = text.trim()

        val plIdx = trimmed.indexOf(PREFIX_PLAYLIST)
        val sgIdx = trimmed.indexOf(PREFIX_SONG)

        return try {
            when {
                plIdx >= 0 -> decodePlaylist(trimmed.substring(plIdx + PREFIX_PLAYLIST.length))
                sgIdx >= 0 -> decodeSong(trimmed.substring(sgIdx + PREFIX_SONG.length))
                else -> Decoded.Invalid("未识别的分享码")
            }
        } catch (e: Exception) {
            Decoded.Invalid("解析失败：${e.message}")
        }
    }

    private fun decodePlaylist(base64Part: String): Decoded {
        val cleaned = extractToken(base64Part)
        val json = String(
            Base64.decode(cleaned, Base64.NO_WRAP or Base64.URL_SAFE),
            Charsets.UTF_8
        )
        val root = JSONObject(json)
        val name = root.optString("name").ifBlank { "导入的歌单" }
        val arr = root.optJSONArray("songs") ?: JSONArray()
        val songs = ArrayList<Song>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            songs.add(
                Song(
                    id = o.optString("id"),
                    source = o.optString("source"),
                    name = o.optString("name"),
                    artist = o.optString("artist"),
                    album = o.optString("album"),
                    sign = o.optString("sign")
                )
            )
        }
        if (songs.isEmpty()) return Decoded.Invalid("歌单里没有歌曲")
        return Decoded.Playlist(name, songs)
    }

    private fun decodeSong(base64Part: String): Decoded {
        val cleaned = extractToken(base64Part)
        val json = String(
            Base64.decode(cleaned, Base64.NO_WRAP or Base64.URL_SAFE),
            Charsets.UTF_8
        )
        val o = JSONObject(json)
        val song = Song(
            id = o.optString("id"),
            source = o.optString("source"),
            name = o.optString("name"),
            artist = o.optString("artist"),
            album = o.optString("album"),
            sign = o.optString("sign")
        )
        if (song.id.isBlank() || song.source.isBlank()) {
            return Decoded.Invalid("歌曲信息不完整")
        }
        return Decoded.Single(song)
    }

    private fun extractToken(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when {
                c.isLetterOrDigit() || c == '-' || c == '_' || c == '=' -> sb.append(c)
                else -> if (sb.isNotEmpty()) break
            }
        }
        return sb.toString()
    }
}