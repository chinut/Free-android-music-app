package com.example.music.data.tv

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 注入到 WebView 的原生桥，对应 utao 的 `addJavascriptInterface(jsInterface, "_api")`。
 *
 * 前端（tv-web 的 common.js `_apiX`）会同步调用这些方法，因此所有方法都必须在
 * JavascriptInterface 线程上同步返回字符串 —— 不能切主线程。
 */
class TvWebBridge(
    private val context: Context,
    private val onEvent: (TvEvent) -> Unit,
) {

    private companion object {
        const val TAG = "TvWebBridge"
    }

    /** 前端通知原生的事件。 */
    sealed interface TvEvent {
        /** 前端请求原生打开播放器全屏。 */
        data object FullscreenRequested : TvEvent

        /** 直播画质列表变化。 */
        data class VideoQuality(val json: String) : TvEvent

        /** 请求回到电视首页（频道列表）。 */
        data object ExitPlayer : TvEvent
    }

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // ==================== 基础 ====================

    @JavascriptInterface
    fun toast(message: String?) {
        if (message.isNullOrBlank()) return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 前端 → 原生。service 与 utao 保持一致。
     * data 是 JSON 字符串（`_apiX.msg` 会 stringify）。
     */
    @JavascriptInterface
    fun message(service: String?, data: String?) {
        Log.d(TAG, "message service=$service data=$data")
        when (service) {
            "activity" -> {
                // 原版用于在“网页浏览器 / 电视直播”之间切换，本 App 直接退出播放页
                if (data == "live" || data == "douyin") onEvent(TvEvent.ExitPlayer)
            }

            "videoQuality" -> {
                Log.i(TAG, "收到画质列表: $data")
                onEvent(TvEvent.VideoQuality(data ?: "[]"))
            }

            "menu", "menuShow" -> Unit // 手机端没有遥控器菜单，忽略

            "history.save", "history.update" -> Unit // 手机端不做播放历史持久化

            "clearCache" -> {
                onEvent(TvEvent.ExitPlayer)
            }

            "closeApp" -> onEvent(TvEvent.ExitPlayer)

            "app" -> Unit

            "redirect", "click", "js", "loginQr", "sessionStorage" -> Unit

            else -> Log.d(TAG, "未处理的 service: $service")
        }
    }

    /**
     * 同步查询。对应 utao 的 queryByService。
     * 本 App 不使用本地服务，queryIp / queryHistory / querySysInfo 返回安全缺省值。
     */
    @JavascriptInterface
    fun queryByService(service: String?, extParam: String?): String? {
        Log.d(TAG, "queryByService service=$service param=$extParam")
        return when (service) {
            "queryIp" -> "127.0.0.1"
            "queryHistory" -> "[]"
            "querySysInfo" -> JSONObject().apply {
                put("versionCode", 0)
                put("versionName", "")
                put("haveNew", false)
                put("sys64", true)
                put("x86", false)
                put("x5Ok", true)
                put("deviceId", "")
                put("openOkMenu", false)
                put("cacheSize", "0")
                put("resVersion", "")
            }.toString()

            else -> null
        }
    }

    // ==================== 网络代理 ====================
    // 前端通过 _api.getJson / getHtml / postJson 发请求，由原生带自定义头（Referer/UA）转发，
    // 绕开浏览器对 Referer / Origin 等 forbidden header 的限制。

    @JavascriptInterface
    fun getJson(url: String?, header: String?): String {
        val u = url ?: return "500"
        if (!u.startsWith("http")) return readLocal(u)
        return try {
            val req = Request.Builder()
                .url(u)
                .applyHeaders(header)
                .get()
                .build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) "500" else resp.body?.string() ?: "500"
            }
        } catch (e: Exception) {
            Log.w(TAG, "getJson 失败 $u : ${e.message}")
            "500"
        }
    }

    @JavascriptInterface
    fun getHtml(url: String?, header: String?): String {
        val u = url ?: return "500"
        if (!u.startsWith("http")) return readLocal(u)
        return try {
            val req = Request.Builder()
                .url(u)
                .applyHeaders(header)
                .get()
                .build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) "500" else resp.body?.string() ?: "500"
            }
        } catch (e: Exception) {
            Log.w(TAG, "getHtml 失败 $u : ${e.message}")
            "500"
        }
    }

    @JavascriptInterface
    fun postJson(url: String?, header: String?, requestBody: String?): String {
        val u = url ?: return "500"
        if (!u.startsWith("http")) return readLocal(u)
        return try {
            val body = (requestBody ?: "").toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder()
                .url(u)
                .applyHeaders(header)
                .post(body)
                .build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) "500" else resp.body?.string() ?: "500"
            }
        } catch (e: Exception) {
            Log.w(TAG, "postJson 失败 $u : ${e.message}")
            "500"
        }
    }

    /** 非 http 开头的地址视为 assets 内资源，对应 utao 的 FileUtil.readExt。 */
    private fun readLocal(path: String): String {
        val relative = path.trimStart('/')
        val text = TvAssets.readWebText(context, relative)
        return text.ifEmpty { "500" }
    }

    private fun Request.Builder.applyHeaders(headerJson: String?): Request.Builder {
        if (headerJson.isNullOrBlank()) return this
        try {
            val obj = JSONObject(headerJson)
            val names = obj.keys()
            while (names.hasNext()) {
                val rawName = names.next()
                val value = obj.optString(rawName)
                if (value.isEmpty()) continue
                // 前端自定义的伪头，映射成真实请求头
                val realName = when (rawName.lowercase()) {
                    "tv-ref" -> "Referer"
                    "tv-ori" -> "Origin"
                    "user-agentx" -> "User-Agent"
                    "rel-url" -> continue
                    else -> rawName
                }
                try {
                    header(realName, value)
                } catch (e: Exception) {
                    // 少数非法头名直接跳过
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "解析 header 失败: $headerJson")
        }
        return this
    }

    /** 预留：有些站点会通过注入的 JS 回调原生按键，本 App 暂不需要。 */
    @JavascriptInterface
    fun key(keyCode: String?): Boolean = false

    @JavascriptInterface
    fun keyNum(keyCode: String?): Boolean = false
}
