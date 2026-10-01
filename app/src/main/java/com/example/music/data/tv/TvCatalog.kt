package com.example.music.data.tv

import android.content.Context
import org.json.JSONObject

/** 一个电视频道。 */
data class TvChannel(
    val name: String,
    val url: String,
    val pic: String = "",
    val remark: String = "",
)

/** 一个分类（央视 / 卫视 / 广东 …）。 */
data class TvCategory(
    val name: String,
    val tag: String,
    val channels: List<TvChannel>,
)

/** 在线电视频道目录，数据来自移植自 utao 的 assets/tv-web/js/cctv/tv.json。 */
object TvCatalog {

    private const val ASSET_PATH = "tv-web/js/cctv/tv.json"

    @Volatile
    private var cached: List<TvCategory>? = null

    /** 读取并缓存频道目录。解析失败时返回空列表，不抛异常。 */
    fun load(context: Context): List<TvCategory> {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: parse(readAsset(context, ASSET_PATH)).also { cached = it }
        }
    }

    private fun readAsset(context: Context, path: String): String = try {
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
        ""
    }

    private fun parse(json: String): List<TvCategory> {
        if (json.isBlank()) return emptyList()
        return try {
            val root = JSONObject(json)
            val arr = root.optJSONArray("data") ?: return emptyList()
            val out = ArrayList<TvCategory>(arr.length())
            for (i in 0 until arr.length()) {
                val cat = arr.optJSONObject(i) ?: continue
                val vods = cat.optJSONArray("vods") ?: continue
                val list = ArrayList<TvChannel>(vods.length())
                for (j in 0 until vods.length()) {
                    val v = vods.optJSONObject(j) ?: continue
                    val name = v.optString("name").trim()
                    val url = v.optString("url").trim()
                    if (name.isEmpty() || url.isEmpty()) continue
                    list.add(
                        TvChannel(
                            name = name,
                            url = url,
                            pic = v.optString("pic"),
                            remark = v.optString("remark"),
                        )
                    )
                }
                if (list.isEmpty()) continue
                out.add(
                    TvCategory(
                        name = cat.optString("name", "未命名"),
                        tag = cat.optString("tag", "tag$i"),
                        channels = list,
                    )
                )
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 该地址是否是可以直接交给播放器播放的媒体流。 */
    fun isDirectStream(url: String): Boolean {
        val u = url.lowercase()
        if (u.startsWith("http") && u.contains(".m3u8") && !u.contains("/tv-web/")) return true
        if (u.startsWith("http") && u.contains(".flv") && !u.contains("/tv-web/")) return true
        return false
    }
}
