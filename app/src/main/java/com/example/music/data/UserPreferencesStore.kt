package com.example.music.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class UserPreferencesStore(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // ================= 首次引导 =================

    fun isOnboarded(): Boolean = prefs.getBoolean("onboarded", false)
    fun setOnboarded(v: Boolean) = prefs.edit { putBoolean("onboarded", v) }

    fun getSelectedGenres(): Set<String> =
        prefs.getStringSet("genres", emptySet()) ?: emptySet()

    fun setSelectedGenres(v: Set<String>) =
        prefs.edit { putStringSet("genres", v) }

    fun getSelectedArtists(): Set<String> =
        prefs.getStringSet("artists", emptySet()) ?: emptySet()

    fun setSelectedArtists(v: Set<String>) =
        prefs.edit { putStringSet("artists", v) }

    // ================= ★ 自定义喜欢的歌手 =================

    fun getCustomLikedArtists(): Set<String> =
        prefs.getStringSet("custom_liked_artists", emptySet()) ?: emptySet()

    fun setCustomLikedArtists(v: Set<String>) =
        prefs.edit { putStringSet("custom_liked_artists", v) }

    // ================= 黑名单 =================

    fun getBlacklistArtists(): Set<String> =
        prefs.getStringSet("blacklist_artists", emptySet()) ?: emptySet()

    fun setBlacklistArtists(v: Set<String>) =
        prefs.edit { putStringSet("blacklist_artists", v) }

    fun getBlacklistGenres(): Set<String> =
        prefs.getStringSet("blacklist_genres", emptySet()) ?: emptySet()

    fun setBlacklistGenres(v: Set<String>) =
        prefs.edit { putStringSet("blacklist_genres", v) }

    fun getBlacklistKeywords(): Set<String> =
        prefs.getStringSet("blacklist_keywords", emptySet()) ?: emptySet()

    fun setBlacklistKeywords(v: Set<String>) =
        prefs.edit { putStringSet("blacklist_keywords", v) }

    // ================= 开关 =================

    fun isAutoPlayEnabled(): Boolean = prefs.getBoolean("auto_play_enabled", true)
    fun setAutoPlayEnabled(v: Boolean) = prefs.edit { putBoolean("auto_play_enabled", v) }

    fun isKeepScreenOnEnabled(): Boolean = prefs.getBoolean("keep_screen_on", true)
    fun setKeepScreenOnEnabled(v: Boolean) = prefs.edit { putBoolean("keep_screen_on", v) }

    // ================= 播放历史 =================

    fun recordPlay(artist: String) {
        if (artist.isBlank()) return

        val counts = getArtistPlayCounts().toMutableMap()
        counts[artist] = (counts[artist] ?: 0) + 1
        prefs.edit { putString("artist_play_counts", mapToJson(counts)) }

        val times = getArtistLastPlayed().toMutableMap()
        times[artist] = System.currentTimeMillis()
        prefs.edit { putString("artist_last_played", mapToJson(times)) }
    }

    fun getArtistPlayCounts(): Map<String, Int> {
        val s = prefs.getString("artist_play_counts", null) ?: return emptyMap()
        return try {
            val o = JSONObject(s)
            o.keys().asSequence().associateWith { o.optInt(it) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getArtistLastPlayed(): Map<String, Long> {
        val s = prefs.getString("artist_last_played", null) ?: return emptyMap()
        return try {
            val o = JSONObject(s)
            o.keys().asSequence().associateWith { o.optLong(it) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // ================= 搜索历史 =================

    fun recordSearch(keyword: String) {
        if (keyword.isBlank()) return
        val kw = keyword.trim()
        if (kw.length < 2) return

        val list = getSearchHistory().toMutableList()
        val existing = list.firstOrNull { it.first == kw }
        if (existing != null) {
            list.remove(existing)
            list.add(0, kw to (existing.second + 1))
        } else {
            list.add(0, kw to 1)
        }

        val trimmed = list.take(30)
        val arr = JSONArray()
        trimmed.forEach { (k, c) ->
            arr.put(JSONObject().apply {
                put("kw", k)
                put("c", c)
            })
        }
        prefs.edit { putString("search_history", arr.toString()) }
    }

    fun getSearchHistory(): List<Pair<String, Int>> {
        val s = prefs.getString("search_history", null) ?: return emptyList()
        return try {
            val arr = JSONArray(s)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val kw = o.optString("kw")
                if (kw.isBlank()) null else kw to o.optInt("c")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit {
            remove("artist_play_counts")
            remove("artist_last_played")
            remove("search_history")
        }
    }

    private fun mapToJson(map: Map<String, *>): String {
        val o = JSONObject()
        map.forEach { (k, v) -> o.put(k, v) }
        return o.toString()
    }
}