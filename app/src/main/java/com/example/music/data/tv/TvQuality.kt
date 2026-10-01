package com.example.music.data.tv

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

/** 一路可选画质（由页面上的 `_api.message("videoQuality", [...])` 提供）。 */
data class TvQualityOption(
    val id: String,
    val name: String,
    val level: Int = 0,
)

/** 解析前端上报的画质列表 JSON。格式：[{id,name,level}] */
fun parseQualityOptions(json: String): List<TvQualityOption> {
    if (json.isBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        val out = ArrayList<TvQualityOption>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("id")
            val name = o.optString("name")
            if (id.isEmpty() || name.isEmpty()) continue
            out.add(TvQualityOption(id = id, name = name, level = o.optInt("level", 0)))
        }
        out
    } catch (e: Exception) {
        emptyList()
    }
}

/** 收藏频道 / 记住上次播放的频道。 */
object TvFavorites {

    private const val PREF = "tv_favorites"
    private const val KEY_FAV = "favorites"
    private const val KEY_LAST = "last_channel"

    /** 收藏集合的响应式镜像，UI 订阅它即可随收藏变化刷新。 */
    private val _flow = MutableStateFlow<Set<String>>(emptySet())
    val flow: StateFlow<Set<String>> = _flow.asStateFlow()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    /** 收藏的频道 url 集合。 */
    fun urls(context: Context): Set<String> {
        if (_flow.value.isEmpty()) {
            _flow.value = prefs(context).getStringSet(KEY_FAV, emptySet())?.toSet() ?: emptySet()
        }
        return _flow.value
    }

    fun isFavorite(context: Context, url: String): Boolean = urls(context).contains(url)

    /** 切换收藏，返回切换后是否已收藏。 */
    fun toggle(context: Context, url: String): Boolean {
        val set = urls(context).toMutableSet()
        val nowFavorite = if (set.contains(url)) {
            set.remove(url)
            false
        } else {
            set.add(url)
            true
        }
        prefs(context).edit().putStringSet(KEY_FAV, set).apply()
        _flow.value = set.toSet()
        return nowFavorite
    }

    /** 记录最后播放的频道（用于下次进入直接续播）。 */
    fun rememberLast(context: Context, channelUrl: String) {
        prefs(context).edit().putString(KEY_LAST, channelUrl).apply()
    }

    fun lastChannelUrl(context: Context): String? =
        prefs(context).getString(KEY_LAST, null)

    /** 按 url 找出频道对象，用于恢复上次播放。 */
    fun findChannel(context: Context, url: String): TvChannel? {
        for (cat in TvCatalog.load(context)) {
            cat.channels.firstOrNull { it.url == url }?.let { return it }
        }
        return null
    }
}
