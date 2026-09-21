package com.example.music.data.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.music.data.api.ApiHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object UpdateChecker {

    // ============================================================
    // ★★★ 修改成你自己的用户名和仓库名 ★★★
    // ============================================================

    /** GitHub 仓库 */
    private const val GITHUB_OWNER = "chinut"
    private const val GITHUB_REPO = "Free-android-music-app"

    /** Gitee 仓库（和 GitHub 同名同结构即可） */
    private const val GITEE_OWNER = "chinut"
    private const val GITEE_REPO = "Free-android-music-app"

    // ============================================================

    /** 更新源配置。priority 越小越优先下载 */
    private data class Source(
        val name: String,
        val apiUrl: String,
        val priority: Int,
        val userAgent: String,
        val acceptHeader: String,
    )

    private val sources = listOf(
        Source(
            name = "Gitee",
            apiUrl = "https://gitee.com/api/v5/repos/$GITEE_OWNER/$GITEE_REPO/releases/latest",
            priority = 0,                 // ★ 国内优先 Gitee
            userAgent = "YinyuekuMusicApp",
            acceptHeader = "application/json",
        ),
        Source(
            name = "GitHub",
            apiUrl = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest",
            priority = 1,                 // 备用 GitHub
            userAgent = "YinyuekuMusicApp",
            acceptHeader = "application/vnd.github+json",
        ),
    )

    /**
     * 同时查询所有源，取版本号最高的作为目标。
     * 返回 null 表示没有更新或全部查询失败。
     */
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? =
        withContext(Dispatchers.IO) {
            // 并发查询所有源
            val results = coroutineScope {
                sources.map { src ->
                    async(Dispatchers.IO) { querySource(src) }
                }.awaitAll()
            }.filterNotNull()

            if (results.isEmpty()) return@withContext null

            // 只保留比当前版本新的
            val newer = results.filter { it.versionCode > currentVersionCode }
            if (newer.isEmpty()) return@withContext null

            // 取 versionCode 最高的作为目标版本
            val latest = newer.maxByOrNull { it.versionCode }!!

            // 收集所有源的下载 URL（按 priority 排序）
            val apkSources = newer
                .filter { it.apkUrl.isNotBlank() }
                .sortedBy { it.priority }
                .map {
                    ApkSource(
                        name = it.sourceName,
                        url = it.apkUrl,
                        priority = it.priority,
                    )
                }

            if (apkSources.isEmpty()) return@withContext null

            UpdateInfo(
                versionCode = latest.versionCode,
                versionName = latest.versionName,
                releaseNotes = latest.releaseNotes,
                apkSources = apkSources,
                apkSize = latest.apkSize,
                releasePageUrl = latest.releasePageUrl,
                primarySource = latest.sourceName,
            )
        }

    /**
     * 查询单个源。
     */
    private fun querySource(src: Source): RawInfo? {
        try {
            val req = Request.Builder()
                .url(src.apiUrl)
                .header("User-Agent", src.userAgent)
                .header("Accept", src.acceptHeader)
                .build()

            ApiHolder.client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JSONObject(body)

                val tag = json.optString("tag_name", "")
                val releaseNotes = json.optString("body", "")
                val releasePageUrl = json.optString("html_url", "")

                // 从描述里解析 versionCode
                val codeRegex = Regex("""versionCode[:\s=]+(\d+)""", RegexOption.IGNORE_CASE)
                val remoteCode = codeRegex.find(releaseNotes)
                    ?.groupValues?.get(1)?.toIntOrNull() ?: 0
                if (remoteCode <= 0) return null

                // 找 assets 里的 apk
                var apkUrl = ""
                var apkSize = 0L
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.optJSONObject(i) ?: continue
                        val name = a.optString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = a.optString("browser_download_url")
                            apkSize = a.optLong("size")
                            break
                        }
                    }
                }

                return RawInfo(
                    sourceName = src.name,
                    priority = src.priority,
                    versionCode = remoteCode,
                    versionName = tag.removePrefix("v").ifBlank { "新版本" },
                    releaseNotes = releaseNotes,
                    apkUrl = apkUrl,
                    apkSize = apkSize,
                    releasePageUrl = releasePageUrl,
                )
            }
        } catch (e: Exception) {
            return null
        }
    }

    private data class RawInfo(
        val sourceName: String,
        val priority: Int,
        val versionCode: Int,
        val versionName: String,
        val releaseNotes: String,
        val apkUrl: String,
        val apkSize: Long,
        val releasePageUrl: String,
    )

    /**
     * 下载 APK。自动按 priority 尝试所有源，任一成功即返回。
     *
     * @param onProgress   下载进度 0f~1f
     * @param onSourceChange 每次切换源时回调，通知 UI 当前在用哪个源
     */
    suspend fun downloadApk(
        context: Context,
        info: UpdateInfo,
        onProgress: (Float) -> Unit,
        onSourceChange: (String) -> Unit = {},
    ): File? = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "updates")
        if (!dir.exists()) dir.mkdirs()
        val apkFile = File(dir, "update-${info.versionCode}.apk")

        // 已下载且大小匹配 → 直接返回
        if (apkFile.exists() && info.apkSize > 0 && apkFile.length() == info.apkSize) {
            return@withContext apkFile
        }

        // 按 priority 排序后逐个尝试
        val sorted = info.apkSources.sortedBy { it.priority }
        for (src in sorted) {
            onSourceChange(src.name)
            onProgress(0f)

            val ok = tryDownloadTo(src.url, apkFile, info.apkSize, onProgress)
            if (ok) return@withContext apkFile

            // 清理失败残留
            if (apkFile.exists()) apkFile.delete()
        }

        // 全部失败
        null
    }

    /**
     * 从指定 URL 下载到目标文件。
     * 成功返回 true（且文件大小 >= 预期 90%）。
     */
    private fun tryDownloadTo(
        url: String,
        dest: File,
        expectedSize: Long,
        onProgress: (Float) -> Unit,
    ): Boolean {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "YinyuekuMusicApp")
                .header("Accept", "*/*")
                .build()

            ApiHolder.client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return false
                val body = resp.body ?: return false
                val total = body.contentLength().let { if (it > 0) it else expectedSize }
                var downloaded = 0L

                body.byteStream().use { input ->
                    FileOutputStream(dest).use { output ->
                        val buf = ByteArray(8192)
                        while (true) {
                            val read = input.read(buf)
                            if (read <= 0) break
                            output.write(buf, 0, read)
                            downloaded += read
                            if (total > 0) {
                                onProgress((downloaded.toFloat() / total).coerceIn(0f, 1f))
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (!dest.exists()) return false

            // 期望大小已知时验证完整性
            if (expectedSize > 0 && dest.length() < expectedSize * 9 / 10) {
                return false
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 调起系统安装器。
     */
    fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}