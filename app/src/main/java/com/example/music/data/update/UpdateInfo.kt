package com.example.music.data.update

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkSources: List<ApkSource>,   // ★ 多个下载源
    val apkSize: Long,
    val releasePageUrl: String,
    val primarySource: String,         // 主源名称（版本号最高的那个）
)

/** 一个 APK 下载源 */
data class ApkSource(
    val name: String,      // "GitHub" / "Gitee"
    val url: String,
    val priority: Int,     // 越小越优先
)