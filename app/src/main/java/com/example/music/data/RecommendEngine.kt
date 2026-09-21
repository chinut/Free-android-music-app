package com.example.music.data

import kotlin.math.min
import kotlin.random.Random

object RecommendEngine {

    val GENRE_ARTISTS: Map<String, List<String>> = mapOf(
        "华语流行" to listOf(
            "周杰伦", "林俊杰", "陈奕迅", "薛之谦", "邓紫棋",
            "李荣浩", "张杰", "汪苏泷", "周深", "张碧晨",
            "王菲", "那英", "李宇春", "华晨宇", "毛不易",
            "张韶涵", "杨丞琳", "蔡依林"
        ),
        "华语摇滚" to listOf(
            "五月天", "Beyond", "汪峰", "许巍", "郑钧",
            "GALA", "痛仰", "新裤子", "逃跑计划", "刺猬",
            "黑豹乐队", "唐朝乐队", "万能青年旅店", "草东没有派对"
        ),
        "华语民谣" to listOf(
            "毛不易", "赵雷", "陈粒", "宋冬野", "李志",
            "朴树", "房东的猫", "陈绮贞", "老狼", "好妹妹",
            "马頔", "尧十三", "万晓利", "钟立风"
        ),
        "华语说唱" to listOf(
            "GAI", "马思唯", "法老", "艾热", "Bridge",
            "Jony J", "Higher Brothers", "VAVA", "万妮达",
            "功夫胖", "谢帝", "派克特", "艾福杰尼"
        ),
        "古风国风" to listOf(
            "周深", "银临", "河图", "许嵩", "音阙诗听",
            "五音JW", "不才", "HITA", "李常超", "Winky诗",
            "双笙", "特曼", "排骨教主"
        ),
        "粤语经典" to listOf(
            "陈奕迅", "张学友", "Beyond", "张国荣", "容祖儿",
            "李克勤", "梅艳芳", "许冠杰", "陈百强", "谭咏麟",
            "Twins", "刘德华", "郭富城", "杨千嬅"
        ),
        "华语R&B" to listOf(
            "陶喆", "方大同", "周杰伦", "王力宏", "林俊杰",
            "袁娅维", "余佳运", "J.Sheon", "ØZI", "Karencici"
        ),
        "欧美流行" to listOf(
            "Taylor Swift", "Ed Sheeran", "Ariana Grande",
            "Justin Bieber", "Dua Lipa", "Billie Eilish",
            "Bruno Mars", "Adele", "Selena Gomez",
            "Charlie Puth", "Shawn Mendes", "Camila Cabello",
            "Olivia Rodrigo", "Harry Styles", "Miley Cyrus"
        ),
        "欧美摇滚" to listOf(
            "Coldplay", "Imagine Dragons", "Maroon 5",
            "Linkin Park", "Green Day", "Queen",
            "The Beatles", "Radiohead", "Arctic Monkeys",
            "Twenty One Pilots", "OneRepublic", "The 1975",
            "Red Hot Chili Peppers", "Muse"
        ),
        "欧美说唱" to listOf(
            "Eminem", "Drake", "Kanye West", "Kendrick Lamar",
            "Travis Scott", "Post Malone", "J. Cole",
            "Lil Nas X", "21 Savage", "Future",
            "Jack Harlow", "Doja Cat", "Nicki Minaj"
        ),
        "欧美电子" to listOf(
            "Alan Walker", "Avicii", "Martin Garrix", "Kygo",
            "David Guetta", "Calvin Harris", "The Chainsmokers",
            "Marshmello", "Zedd", "ILLENIUM",
            "Tiësto", "Deadmau5", "Skrillex", "Diplo"
        ),
        "欧美R&B" to listOf(
            "The Weeknd", "Bruno Mars", "Frank Ocean",
            "Usher", "Chris Brown", "SZA",
            "H.E.R.", "Summer Walker", "Daniel Caesar",
            "Brent Faiyaz", "Giveon"
        ),
        "日本流行" to listOf(
            "米津玄師", "YOASOBI", "Official髭男dism",
            "Aimer", "King Gnu", "Vaundy", "Ado",
            "あいみょん", "宇多田ヒカル", "back number",
            "RADWIMPS", "桑田佳祐", "SPITZ", "Mr.Children"
        ),
        "日本动漫" to listOf(
            "LiSA", "Aimer", "ClariS", "Kalafina",
            "EGOIST", "Sound Horizon", "fripSide",
            "supercell", "yanaginagi", "GARNiDELiA",
            "ReoNa", "MYTH & ROID", "Eir Aoi"
        ),
        "韩国流行" to listOf(
            "BTS", "BLACKPINK", "IU", "TWICE", "EXO",
            "Red Velvet", "SEVENTEEN", "Stray Kids",
            "NewJeans", "aespa", "ITZY", "(G)I-DLE",
            "NCT", "MAMAMOO", "TXT"
        ),
        "韩国说唱" to listOf(
            "Zico", "Jay Park", "Epik High", "BewhY",
            "Sik-K", "DEAN", "Crush", "Zion.T",
            "DPR LIVE", "pH-1", "GRAY", "Loco"
        ),
        "经典老歌" to listOf(
            "邓丽君", "张国荣", "梅艳芳", "罗大佑", "李宗盛",
            "蔡琴", "Beyond", "陈百强", "童安格", "齐秦",
            "凤飞飞", "费玉清", "苏芮", "陈淑桦"
        ),
        "爵士蓝调" to listOf(
            "Norah Jones", "Michael Bublé", "Diana Krall",
            "Frank Sinatra", "Louis Armstrong", "Ella Fitzgerald",
            "Nat King Cole", "Billie Holiday", "Ray Charles"
        ),
        "纯音乐" to listOf(
            "久石让", "坂本龙一", "班得瑞", "Yiruma",
            "Ludovico Einaudi", "Max Richter",
            "Hans Zimmer", "Ólafur Arnalds", "Nils Frahm"
        )
    )

    val ALL_GENRES: List<String> = GENRE_ARTISTS.keys.toList()

    val ALL_ARTISTS: List<String> by lazy {
        GENRE_ARTISTS.values.flatten().distinct().sorted()
    }

    fun isArtistBlocked(
        artist: String,
        blockedArtists: Set<String>,
        blockedGenres: Set<String>,
        blockedKeywords: Set<String>,
    ): Boolean {
        if (artist in blockedArtists) return true

        blockedGenres.forEach { genre ->
            GENRE_ARTISTS[genre]?.let { list ->
                if (artist in list) return true
            }
        }

        if (blockedKeywords.isNotEmpty()) {
            val lower = artist.lowercase()
            blockedKeywords.forEach { kw ->
                if (kw.isNotBlank() && lower.contains(kw.lowercase())) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * 推荐逻辑：
     *  1) 用户偏好（喜欢的风格 + 喜欢的歌手 + 自定义歌手）作为"主力池"
     *  2) 播放历史/搜索历史加权
     *  3) 黑名单过滤
     *  4) 结果里预留 1~2 位"扩展歌手"，帮用户拓展范围
     */
    fun pickArtists(prefs: UserPreferencesStore, topN: Int = 6): List<String> {
        val blockedArtists = prefs.getBlacklistArtists()
        val blockedGenres = prefs.getBlacklistGenres()
        val blockedKeywords = prefs.getBlacklistKeywords()

        fun blocked(a: String) =
            isArtistBlocked(a, blockedArtists, blockedGenres, blockedKeywords)

        // ★ 偏好池（喜欢的歌手 + 自定义歌手 + 喜欢的风格下的歌手）
        val primaryPool = mutableSetOf<String>()
        prefs.getSelectedArtists().forEach { if (!blocked(it)) primaryPool.add(it) }
        prefs.getCustomLikedArtists().forEach { if (!blocked(it)) primaryPool.add(it) }
        prefs.getSelectedGenres().forEach { genre ->
            GENRE_ARTISTS[genre]?.forEach { if (!blocked(it)) primaryPool.add(it) }
        }

        // 若偏好池为空，降级为默认
        if (primaryPool.isEmpty()) {
            val fallback = listOf(
                "周杰伦", "林俊杰", "陈奕迅", "Taylor Swift",
                "邓紫棋", "米津玄師", "五月天", "IU"
            ).filter { !blocked(it) }
            return fallback.shuffled().take(topN).ifEmpty {
                ALL_ARTISTS.filter { !blocked(it) }.shuffled().take(topN)
            }
        }

        // 加权（偏好池内）+ 历史加权
        val scores = mutableMapOf<String, Double>()
        primaryPool.forEach { a -> scores[a] = 5.0 }

        prefs.getArtistPlayCounts().forEach { (artist, count) ->
            if (!blocked(artist)) {
                val base = scores[artist] ?: 0.0
                scores[artist] = base + min(count, 10) * 0.5
            }
        }

        val lastPlayed = prefs.getArtistLastPlayed()
        val now = System.currentTimeMillis()
        lastPlayed.forEach { (artist, t) ->
            if (!blocked(artist) && now - t < 7L * 86400 * 1000) {
                val base = scores[artist] ?: 0.0
                scores[artist] = base + 2.0
            }
        }

        prefs.getSearchHistory().forEach { (kw, count) ->
            if (!blocked(kw)) {
                val base = scores[kw] ?: 0.0
                scores[kw] = base + min(count, 3) * 0.5
            }
        }

        scores.forEach { (k, v) ->
            scores[k] = v + Random.nextDouble(0.0, 1.2)
        }

        // ★ 从偏好池加权随机抽 N-1 ~ N-2 位
        val expandCount = if (topN >= 5 && Random.nextDouble() < 0.5) 2 else 1
        val primaryCount = (topN - expandCount).coerceAtLeast(1)

        val result = mutableListOf<String>()
        val pool = scores.toMutableMap()
        repeat(primaryCount) {
            if (pool.isEmpty()) return@repeat
            val total = pool.values.sum()
            if (total <= 0.0) {
                result.add(pool.keys.random())
                pool.remove(result.last())
                return@repeat
            }
            var r = Random.nextDouble() * total
            var picked: String? = null
            for ((k, v) in pool) {
                r -= v
                if (r <= 0) { picked = k; break }
            }
            if (picked == null) picked = pool.keys.random()
            result.add(picked)
            pool.remove(picked)
        }

        // ★ 从"扩展池"（不在偏好里、不被屏蔽）里抽 1~2 位
        val expandPool = ALL_ARTISTS
            .filter { it !in primaryPool && !blocked(it) }
            .shuffled()

        repeat(expandCount) { idx ->
            if (idx < expandPool.size) {
                result.add(expandPool[idx])
            }
        }

        // 扩展歌手穿插到中间，而不是都堆在末尾
        if (result.size >= 3) {
            val primaryList = result.take(primaryCount)
            val expandList = result.drop(primaryCount)
            val mixed = primaryList.toMutableList()
            expandList.forEachIndexed { i, artist ->
                val pos = Random.nextInt(1, mixed.size + 1).coerceAtMost(mixed.size)
                mixed.add(pos.coerceAtMost(mixed.size), artist)
            }
            return mixed.distinct().take(topN)
        }

        return result.distinct().take(topN)
    }

    fun interleave(songs: List<Song>): List<Song> {
        if (songs.size <= 2) return songs
        val groups = songs.groupBy { it.artist }
            .mapValues { (_, list) -> list.toMutableList() }
            .toMutableMap()
        val result = ArrayList<Song>(songs.size)
        while (groups.isNotEmpty()) {
            val keys = groups.keys.toList()
            for (k in keys) {
                val list = groups[k] ?: continue
                if (list.isNotEmpty()) {
                    result.add(list.removeAt(0))
                }
                if (list.isEmpty()) groups.remove(k)
            }
        }
        return result
    }
}