package com.example.music.data.tv

import android.content.Context

/** 电视遥控的连接设置（地址 / 端口 / 口令 / 已选电视）。 */
class TvRemotePrefs(context: Context) {

    private val prefs = context.getSharedPreferences("tv_remote", Context.MODE_PRIVATE)

    /**
     * 当前使用的电视地址。
     *
     * 家里有多台电视时，这里存的是用户挑中的那一台；下次进页面直接连它。
     */
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

    /**
     * 是否已由用户明确选定过电视。
     *
     * 用于区分两种情况：
     * - 用户选过 → 直接连它，即使扫到多台也不再打扰
     * - 只有自动发现留下的地址 → 多台时仍应让用户挑一次
     */
    var userChosen: Boolean
        get() = prefs.getBoolean(KEY_USER_CHOSEN, false)
        set(v) { prefs.edit().putBoolean(KEY_USER_CHOSEN, v).apply() }

    /** 是否已经有可用地址。 */
    val configured: Boolean get() = host.isNotBlank()

    /**
     * 记住用户挑中的这台电视。
     * @param byUser 是否为用户主动选择（多台时挑选的结果）
     */
    fun select(host: String, byUser: Boolean) {
        this.host = host
        this.userChosen = byUser
    }

    /** 忘记当前电视，下次重新发现/挑选。 */
    fun forget() {
        prefs.edit().remove(KEY_HOST).putBoolean(KEY_USER_CHOSEN, false).apply()
    }

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
        private const val KEY_USER_CHOSEN = "user_chosen"
    }
}
