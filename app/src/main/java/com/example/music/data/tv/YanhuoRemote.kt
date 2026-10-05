package com.example.music.data.tv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 焰火TV 网络遥控客户端。
 *
 * 零依赖（只用 JDK + org.json），把手机上的操作变成 HTTP 请求发给电视上的「焰火TV」。
 *
 * ```kotlin
 * val tv = YanhuoRemote("192.168.1.23", 8899)
 * if (tv.ping()) {
 *     tv.key(YanhuoRemote.Key.DOWN)
 *     tv.volumeDelta(+2)
 *     val st = tv.status()
 * }
 * ```
 *
 * 所有方法都是挂起函数（内部切到 IO 线程），返回值不会抛异常，失败一律返回 null / false。
 * 协议详见 TV 工程的「网络遥控协议 v1」。
 */
class YanhuoRemote(
    /** 电视的局域网 IP。 */
    private val host: String,
    /** 端口。默认 8899，与电视上「设置 → 手机网页调试 → 调试端口」一致。 */
    private val port: Int = 8899,
    /** 口令。电视上没设口令时留空即可。 */
    private val token: String = "",
    /** 单次请求超时（毫秒）。局域网下 3 秒足够。 */
    private val timeoutMs: Int = 3000,
) {

    /** 遥控器按键。值就是 Android KeyEvent 键码。 */
    enum class Key(val code: Int) {
        UP(19), DOWN(20), LEFT(21), RIGHT(22),
        OK(23), BACK(4), MENU(82),
        PLAY(126), PAUSE(127), PLAY_PAUSE(85), STOP(86),
        NEXT(87), PREV(88), FORWARD(90), REWIND(89),
        VOL_UP(24), VOL_DOWN(25), MUTE(164);

        val wire: String get() = "keyCode=$code"
    }

    /** 电视返回的状态。 */
    data class Status(
        /** 电视当前在哪个界面：home / vod / live / settings / playing。 */
        val screen: String,
        val volume: Int,
        val volumeMax: Int,
        val muted: Boolean,
        /** 电视端的时间戳（毫秒）。用它做事件轮询的游标。 */
        val now: Long,
    )

    /** 一次按键事件（电视上真实按下的，包含实体遥控器按的）。 */
    data class KeyEvent(val time: Long, val keyCode: Int)

    // ==================== 探测 ====================

    /** 探测电视是否在线。 */
    suspend fun ping(): Boolean = get("/api/remote/ping") != null

    /** 探测并返回协议版本；不在线返回 0。 */
    suspend fun protocolVersion(): Int {
        val body = get("/api/remote/ping") ?: return 0
        return runCatching { JSONObject(body).optInt("protocol", 0) }.getOrDefault(0)
    }

    /**
     * 探测结果：用来区分家里多台电视。
     *
     * 向后兼容：老版本电视端只返回 `app` / `protocol` / `screen`，
     * 新版本额外返回 `name`（用户设的电视名）和 `port`（实际监听端口）。
     * 缺哪个字段都不影响使用，只是退回到旧行为。
     */
    data class PingInfo(
        /** 设备自报的名字。新协议是 "name"，老协议是 "app"。 */
        val name: String = "",
        val protocol: Int = 0,
        val screen: String = "",
        /** 电视实际监听的端口；老版本不返回，为 0。 */
        val port: Int = 0,
    ) {
        /** 展示用的标题；两个字段都没有时退回默认名。 */
        val title: String get() = name.ifBlank { DEFAULT_NAME }

        val hasPort: Boolean get() = port in 1..65535

        companion object {
            const val DEFAULT_NAME = "焰火TV"
        }
    }

    /**
     * 探测并读回设备标识；不在线返回 null。
     *
     * 多台电视时靠这个区分：把每台返回的 [PingInfo] 展示给用户挑。
     */
    suspend fun pingInfo(): PingInfo? {
        val body = get("/api/remote/ping") ?: return null
        return runCatching {
            val o = JSONObject(body)
            // name 优先（新协议），退回 app（老协议）
            val name = o.optString("name", "").ifBlank { o.optString("app", "") }
            PingInfo(
                name = name,
                protocol = o.optInt("protocol", 0),
                screen = o.optString("screen", ""),
                port = o.optInt("port", 0),
            )
        }.getOrNull()
    }

    // ==================== 状态 ====================

    /** 读当前状态（音量、界面）。 */
    suspend fun status(): Status? {
        val body = get("/api/remote/status") ?: return null
        return runCatching {
            val o = JSONObject(body)
            Status(
                screen = o.optString("screen", "unknown"),
                volume = o.optInt("volume", 0),
                volumeMax = o.optInt("volumeMax", 15),
                muted = o.optBoolean("muted", false),
                now = o.optLong("now", 0L),
            )
        }.getOrNull()
    }

    // ==================== 按键 ====================

    /** 按一下遥控器。返回电视是否消费了这个键。 */
    suspend fun key(k: Key): Boolean = key(k.code)

    /** 按任意 Android 键码。 */
    suspend fun key(keyCode: Int): Boolean {
        val body = post("/api/remote/key", "keyCode=$keyCode") ?: return false
        return runCatching { JSONObject(body).optBoolean("consumed", false) }.getOrDefault(false)
    }

    /** 长按支持：先发 down、稍后发 up，中间电视会持续响应。 */
    suspend fun keyDown(keyCode: Int): Boolean = sendKey(keyCode, "down")

    suspend fun keyUp(keyCode: Int): Boolean = sendKey(keyCode, "up")

    private suspend fun sendKey(keyCode: Int, action: String): Boolean {
        val body = post("/api/remote/key", "keyCode=$keyCode&action=$action") ?: return false
        return runCatching { JSONObject(body).optBoolean("ok", false) }.getOrDefault(false)
    }

    /** 发送一段文字到电视当前的输入框。返回电视接受了几个字符。 */
    suspend fun text(text: String): Int {
        val enc = URLEncoder.encode(text, "UTF-8")
        val body = post("/api/remote/text", "text=$enc") ?: return 0
        return runCatching { JSONObject(body).optInt("accepted", 0) }.getOrDefault(0)
    }

    // ==================== 音量 ====================

    /** 相对调音量。delta 为正加、为负减。 */
    suspend fun volumeDelta(delta: Int): Status? {
        post("/api/remote/volume", "delta=$delta")
        return status()
    }

    /** 设绝对音量。value 会被电视钳制到 [0, volumeMax]。 */
    suspend fun setVolume(value: Int): Status? {
        post("/api/remote/volume", "value=$value")
        return status()
    }

    /** 静音 / 取消静音。 */
    suspend fun setMuted(muted: Boolean): Status? {
        post("/api/remote/mute", "muted=${if (muted) 1 else 0}")
        return status()
    }

    suspend fun volumeUp(): Status? = volumeDelta(1)
    suspend fun volumeDown(): Status? = volumeDelta(-1)

    // ==================== 事件回显 ====================

    /**
     * 拉取电视上真实按下的按键事件（含实体遥控器）。
     *
     * @param since 上次拿到的时间戳
     * @param waitMs 长轮询等待毫秒数。传 0 立即返回。
     * @return (事件列表, 下次该传的 since)
     */
    suspend fun pollEvents(since: Long, waitMs: Int = 0): Pair<List<KeyEvent>, Long> {
        val body = get("/api/remote/events?since=$since&wait=$waitMs")
            ?: return emptyList<KeyEvent>() to since
        return runCatching {
            val o = JSONObject(body)
            val arr = o.optJSONArray("events")
            val list = ArrayList<KeyEvent>(arr?.length() ?: 0)
            for (i in 0 until (arr?.length() ?: 0)) {
                val e = arr!!.optJSONObject(i) ?: continue
                list += KeyEvent(e.optLong("t"), e.optInt("keyCode"))
            }
            list to o.optLong("now", since)
        }.getOrDefault(emptyList<KeyEvent>() to since)
    }

    // ==================== HTTP 底层 ====================

    private suspend fun get(path: String): String? = withContext(Dispatchers.IO) {
        request(path, null)
    }

    private suspend fun post(path: String, form: String): String? = withContext(Dispatchers.IO) {
        request(path, form)
    }

    private fun request(path: String, form: String?): String? = runCatching {
        val full = buildUrl(host, port, token, path)
        val conn = (URL(full).openConnection() as HttpURLConnection).apply {
            requestMethod = if (form == null) "GET" else "POST"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs + 2000
            useCaches = false
            if (form != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
        }
        if (form != null) {
            conn.outputStream.use { it.write(form.toByteArray(Charsets.UTF_8)) }
        }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        conn.disconnect()
        if (code in 200..299) text else null
    }.getOrNull()

    companion object {
        /**
         * 拼出完整请求地址。口令非空时以 query 参数附上（POST 也一样，协议两种都接受）。
         * 抽成纯函数便于单测：口令编码写错会导致「明明填了口令却一直 401」。
         */
        fun buildUrl(host: String, port: Int, token: String, path: String): String = buildString {
            append("http://").append(host).append(':').append(port).append(path)
            if (token.isNotBlank()) {
                append(if (path.contains('?')) '&' else '?')
                append("token=").append(URLEncoder.encode(token, "UTF-8"))
            }
        }
    }
}
