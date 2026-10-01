package com.example.music.data.tv

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

/**
 * 读取 assets/tv-web 下的前端资源。
 *
 * utao 的做法是运行时把 tv-web 解压到 filesDir，这里改成直接从 assets 读取并做内存缓存，
 * 省掉一次拷贝，也避免升级后残留旧文件。
 */
object TvAssets {

    /** tv-web 资源在 assets 中的根目录名。 */
    const val ROOT = "tv-web"

    private val cache = ConcurrentHashMap<String, ByteArray>()

    /** 把任意包含 "tv-web" 的地址归一化成 assets 内的相对路径。 */
    fun normalize(url: String): String? {
        val idx = url.indexOf(ROOT)
        if (idx < 0) return null
        var path = url.substring(idx + ROOT.length)
        // 去掉 query / fragment
        path = path.substringBefore('?').substringBefore('#')
        while (path.startsWith("/")) path = path.substring(1)
        if (path.isEmpty()) path = "index.html"
        return path
    }

    /** 按 assets 相对路径读取文本，例如 "tv-web/js/end.js"。 */
    fun readText(context: Context, assetPath: String): String = try {
        context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
        ""
    }

    /** 读取 tv-web 下的文本资源，传 "js/end.js" 这样的相对路径。 */
    fun readWebText(context: Context, relative: String): String =
        readText(context, "$ROOT/${relative.trimStart('/')}")

    /** 读取二进制资源并缓存；找不到返回 null。 */
    fun readBytes(context: Context, relative: String): ByteArray? {
        cache[relative]?.let { return it }
        return try {
            val bytes = context.assets.open("$ROOT/$relative").use { it.readBytes() }
            cache[relative] = bytes
            bytes
        } catch (e: Exception) {
            null
        }
    }

    /** 该 tv-web 相对路径是否存在。 */
    fun exists(context: Context, relative: String): Boolean = try {
        context.assets.open("$ROOT/$relative").use { true }
    } catch (e: Exception) {
        false
    }

    /** 根据扩展名推断 MIME 类型。 */
    fun mimeOf(path: String): String = when (path.substringAfterLast('.', "").lowercase()) {
        "html", "htm" -> "text/html"
        "js", "mjs" -> "text/javascript"
        "css" -> "text/css"
        "json" -> "application/json"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "svg" -> "image/svg+xml"
        "woff2" -> "font/woff2"
        "woff" -> "font/woff"
        "ttf" -> "font/ttf"
        "ico" -> "image/x-icon"
        "m3u8" -> "application/vnd.apple.mpegurl"
        "ts" -> "video/mp2t"
        else -> "application/octet-stream"
    }
}
