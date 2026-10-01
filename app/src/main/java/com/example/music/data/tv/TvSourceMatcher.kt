package com.example.music.data.tv

/**
 * 同频道多源匹配。
 *
 * 频道目录里同一家电视台常出现在多个分类，且指向不同源：
 *   [央视] CCTV-6 电影   -> https://tv.cctv.com/live/cctv6/
 *   [央视源2] CCTV6 电影 -> https://www.yangshipin.cn/tv/home?pid=600108442
 * 某个源失效时就自动换到下一个，而不是直接告诉用户"播不了"。
 */
object TvSourceMatcher {

    /**
     * 找出与 [channel] 同名的所有源，[channel] 自己排第一。
     * 同名判定做过归一化：忽略空格/连字符/全角空格，并去掉括号补充说明。
     */
    fun sourcesOf(channel: TvChannel, categories: List<TvCategory>): List<TvChannel> {
        val key = normalize(channel.name)
        val seenUrls = LinkedHashSet<String>()
        val result = ArrayList<TvChannel>()

        for (cat in categories) {
            for (ch in cat.channels) {
                if (normalize(ch.name) != key) continue
                if (seenUrls.add(ch.url)) result.add(ch)
            }
        }

        // 用户点的那个必须排在最前
        val idx = result.indexOfFirst { it.url == channel.url }
        when {
            idx > 0 -> {
                val pick = result.removeAt(idx)
                result.add(0, pick)
            }
            idx < 0 -> result.add(0, channel)
        }
        return result
    }

    /** 频道名归一化，用于跨分类识别"同一个台"。 */
    fun normalize(name: String): String = name
        .replace(" ", "")
        .replace("\u3000", "")   // 全角空格
        .replace("-", "")
        .replace("—", "")
        .replace("－", "")
        .replace(Regex("[（(].*?[)）]"), "")
        .trim()
        .lowercase()
}
