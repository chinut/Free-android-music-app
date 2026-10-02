package com.example.music.data.tv

import android.content.Context

/** 电视遥控的连接设置（IP / 端口 / 口令）。 */
class TvRemotePrefs(context: Context) {

    private val prefs = context.getSharedPreferences("tv_remote", Context.MODE_PRIVATE)

    var host: String
        get() = prefs.getString(KEY_HOST, "") ?: ""
        set(v) { prefs.edit().putString(KEY_HOST, v.trim()).apply() }

    var port: Int
        get() = prefs.getInt(KEY_PORT, DEFAULT_PORT)
        set(v) { prefs.edit().putInt(KEY_PORT, v.coerceIn(1, 65535)).apply() }

    /** 电视上没设口令时留空。 */
    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(v) { prefs.edit().putString(KEY_TOKEN, v.trim()).apply() }

    /** 是否已经填过地址。 */
    val configured: Boolean get() = host.isNotBlank()

    /** 按当前设置构造客户端；未配置地址时返回 null。 */
    fun client(): YanhuoRemote? {
        if (!configured) return null
        return YanhuoRemote(host = host, port = port, token = token)
    }

    companion object {
        const val DEFAULT_PORT = 8899
        private const val KEY_HOST = "host"
        private const val KEY_PORT = "port"
        private const val KEY_TOKEN = "token"
    }
}
