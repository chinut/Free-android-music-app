package com.example.music.data.tv

/**
 * 频道地址归一化。
 *
 * tv.json 里大量频道用的是「土拨鼠包装页」：
 *   https://<任意站点>/tv-web/live.html?url=<真实流地址>
 * 这些地址依赖 tv.utao.tv 做资源重定向，而该域名已经解析不了，直接加载必然失败。
 * 这里把内层真实地址抽出来，交给原生播放器或本地播放页处理。
 */
object TvStreamUrl {

    /** 内层真实地址的查询参数名。 */
    private const val WRAP_KEY = "url"

    /** 取内层真实地址；不是包装页就原样返回。 */
    fun unwrap(raw: String): String {
        var cur = raw.trim()
        // 最多解 3 层，防畸形嵌套
        repeat(3) {
            val inner = unwrapOnce(cur) ?: return cur
            if (inner == cur) return cur
            cur = inner
        }
        return cur
    }

    private fun unwrapOnce(url: String): String? {
        val idx = url.indexOf("$WRAP_KEY=", ignoreCase = true)
        if (idx < 0) return null
        // 只在本地播放页里解包，避免误伤普通站点自身带 url= 的页面
        if (!isLocalPlayerPage(url)) return null
        val value = url.substring(idx + WRAP_KEY.length + 1)
            .substringBefore('&')
            .trim()
        if (value.isEmpty()) return null
        return runCatching { java.net.URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
    }

    /** 是不是 tv-web 自带的播放页。 */
    fun isLocalPlayerPage(url: String): Boolean =
        url.contains("tv-web") && (url.contains("live.html") || url.contains("live.js"))

    /** 内层地址是否是可直连的媒体流。 */
    fun isDirectMedia(url: String): Boolean {
        val u = url.lowercase()
        if (!u.startsWith("http")) return false
        if (isLocalPlayerPage(u)) return false
        if (u.contains(".m3u8")) return true
        if (u.contains(".flv")) return true
        if (u.contains(".mp4")) return true
        // 有些流地址不带扩展名，但本身是 m3u8 接口
        if (u.contains("m3u8")) return true
        return false
    }

    /**
     * 直连流地址最终形态。若原本是包装页，把内层真实流抽出来。
     * 返回 null 表示这是需要网页渲染的站点页面（交给 WebView + 注入脚本）。
     */
    fun directMediaOf(raw: String): String? {
        val t = unwrap(raw)
        return if (isDirectMedia(t)) t else null
    }

    /** 桌面版 UA —— 绝大多数站点（含央视网）在桌面版下才有可播的视频。 */
    const val UA_DESKTOP =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    /** 移动版 UA —— 央视频等站点只认移动端。 */
    const val UA_MOBILE =
        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    /**
     * 按站点挑 UA。需求是相反的：
     * - 央视网（tv.cctv.com）对桌面 UA 会回「请移至移动端观看」，但桌面版才有标准 <video>；
     * - 央视频（yangshipin）对移动 UA 会弹「前往央视频客户端」，只认桌面端。
     * 两个都试过，结论就是必须按域名分开给。
     */
    fun userAgentFor(pageUrl: String): String {
        val host = runCatching { java.net.URI(pageUrl).host ?: "" }.getOrDefault("")
        return when {
            host.endsWith("yangshipin.cn") -> UA_DESKTOP
            host.endsWith("cctv.com") -> UA_MOBILE
            else -> UA_DESKTOP
        }
    }

    /**
     * 播放时该带哪些请求头。
     * 国内不少流做了防盗链（没有 Referer 直接 403），
     * 而浏览器禁止 JS 设置 Referer，所以只能在原生侧按域名补。
     */
    fun headersFor(streamUrl: String): Map<String, String> {
        val host = runCatching { java.net.URI(streamUrl).host ?: "" }.getOrDefault("")
        // 直播流用移动端 UA：多数国内流是给手机 App 用的
        val ua = UA_MOBILE
        val referer = when {
            host.endsWith("gstv.com.cn") -> "https://www.gstv.com.cn/"
            host.contains("kankanlive") -> "https://www.kankanews.com/"
            host.contains("hyrtv") -> "https://www.hyrtv.cn/"
            host.contains("cctv") || host.contains("cctvpic") -> "https://tv.cctv.com/"
            host.contains("yangshipin") -> "https://www.yangshipin.cn/"
            host.contains("sctv") -> "https://www.sctv.com/"
            host.contains("jxgdw") -> "https://www.jxntv.cn/"
            host.contains("dztv") -> "https://iapp.dztv.tv/"
            host.contains("chinamcache") -> "https://www.zzjy.gov.cn/"
            else -> null
        }
        return buildMap {
            put("User-Agent", ua)
            if (referer != null) put("Referer", referer)
        }
    }
}
