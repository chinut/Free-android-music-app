package com.example.music.data

/**
 * 情景模式。
 * 用户点击某个场景，自动从该场景对应的歌手池里挑歌播放。
 */
enum class SceneMode(
    val id: String,
    val label: String,
    val emoji: String,
    val description: String,
    val artists: List<String>,
) {
    DRIVING(
        id = "driving",
        label = "驾驶",
        emoji = "🚗",
        description = "动力十足，陪你上路",
        artists = listOf(
            "周杰伦", "GAI", "汪峰", "五月天",
            "Taylor Swift", "Ed Sheeran", "马思唯", "郑钧",
            "Imagine Dragons", "新裤子"
        )
    ),
    SAD(
        id = "sad",
        label = "忧郁",
        emoji = "🌧",
        description = "让音乐陪你沉淀",
        artists = listOf(
            "陈奕迅", "林俊杰", "Adele", "李宗盛",
            "蔡琴", "朴树", "宋冬野", "林宥嘉",
            "Billie Eilish", "毛不易"
        )
    ),
    PARTY(
        id = "party",
        label = "聚会",
        emoji = "🎉",
        description = "嗨起来不解释",
        artists = listOf(
            "The Chainsmokers", "Marshmello", "邓紫棋", "华晨宇",
            "Bruno Mars", "Ariana Grande", "蔡依林", "GALA",
            "Martin Garrix", "Doja Cat"
        )
    ),
    STUDY(
        id = "study",
        label = "学习",
        emoji = "📚",
        description = "专注时刻，安静陪伴",
        artists = listOf(
            "久石让", "Yiruma", "Ludovico Einaudi",
            "坂本龙一", "班得瑞", "Ólafur Arnalds",
            "Max Richter", "Hans Zimmer"
        )
    ),
    SPORT(
        id = "sport",
        label = "运动",
        emoji = "💪",
        description = "燃烧卡路里",
        artists = listOf(
            "Eminem", "Alan Walker", "Avicii", "Martin Garrix",
            "Imagine Dragons", "Jay Park", "Zico",
            "The Chainsmokers", "Travis Scott"
        )
    ),
    SLEEP(
        id = "sleep",
        label = "睡前",
        emoji = "🌙",
        description = "睡前放松一下",
        artists = listOf(
            "陈绮贞", "房东的猫", "Norah Jones",
            "IU", "米津玄師", "Aimer",
            "Billie Eilish", "Lana Del Rey"
        )
    ),
    LOVE(
        id = "love",
        label = "恋爱",
        emoji = "💕",
        description = "甜蜜浪漫时刻",
        artists = listOf(
            "周杰伦", "林俊杰", "Taylor Swift", "Bruno Mars",
            "陶喆", "方大同", "Ed Sheeran", "IU",
            "Shawn Mendes", "Camila Cabello"
        )
    ),
    MEMORY(
        id = "memory",
        label = "怀念",
        emoji = "📼",
        description = "回忆当年的歌",
        artists = listOf(
            "邓丽君", "张国荣", "Beyond", "陈百强",
            "蔡琴", "罗大佑", "张学友", "梅艳芳",
            "李宗盛", "齐秦"
        )
    )
}