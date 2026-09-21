package com.example.music.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

/**
 * 用户偏好 + 播放/搜索历史本地存储（SharedPreferences）。
 */
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

    // ================= 自定义喜欢的歌手 =================

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

    // ================= 自动播放开关 =================

    fun isAutoPlayEnabled(): Boolean = prefs.getBoolean("auto_play_enabled", true)
    fun setAutoPlayEnabled(v: Boolean) = prefs.edit { putBoolean("auto_play_enabled", v) }

    // ================= 屏幕常亮开关 =================

    fun isKeepScreenOnEnabled(): Boolean = prefs.getBoolean("keep_screen_on", true)
    fun setKeepScreenOnEnabled(v: Boolean) = prefs.edit { putBoolean("keep_screen_on", v) }

    // ================= EQ 均衡器 =================

    fun isEQEnabled(): Boolean = prefs.getBoolean("eq_enabled", false)
    fun setEQEnabled(v: Boolean) = prefs.edit { putBoolean("eq_enabled", v) }

    fun getActiveEQId(): String = prefs.getString("eq_active_id", "flat") ?: "flat"
    fun setActiveEQId(id: String) = prefs.edit { putString("eq_active_id", id) }

    /** 读取全部自定义 EQ */
    fun getCustomEQs(): List<EQPreset> {
        val s = prefs.getString("eq_custom_list", null) ?: return emptyList()
        return try {
            val arr = JSONArray(s)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id")
                val name = o.optString("name")
                val bandsArr = o.optJSONArray("bands") ?: return@mapNotNull null
                val bands = (0 until bandsArr.length()).map { bandsArr.optInt(it) }
                if (id.isBlank() || bands.isEmpty()) null
                else EQPreset(id, name, bands, isBuiltIn = false)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomEQ(preset: EQPreset) {
        val list = getCustomEQs().toMutableList()
        val idx = list.indexOfFirst { it.id == preset.id }
        if (idx >= 0) list[idx] = preset else list.add(preset)
        persistCustomEQs(list)
    }

    fun deleteCustomEQ(id: String) {
        val list = getCustomEQs().filterNot { it.id == id }
        persistCustomEQs(list)
    }

    private fun persistCustomEQs(list: List<EQPreset>) {
        val arr = JSONArray()
        list.forEach { p ->
            val bandsArr = JSONArray()
            p.bands.forEach { bandsArr.put(it) }
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("bands", bandsArr)
            })
        }
        prefs.edit { putString("eq_custom_list", arr.toString()) }
    }

    // ================= 播放历史（按歌手统计） =================

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

    // ================= 工具 =================

    private fun mapToJson(map: Map<String, *>): String {
        val o = JSONObject()
        map.forEach { (k, v) -> o.put(k, v) }
        return o.toString()
    }
}