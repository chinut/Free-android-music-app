package com.example.music

import android.app.Application

/**
 * 全局 Application 类。
 * 用于存放进程级的会话状态（如开屏是否显示、自动播放是否触发）。
 */
class MusicApp : Application() {

    /** 本次 APP 进程会话是否已经触发过"自动播放"检查 */
    var autoPlayTriggered: Boolean = false

    /** 本次 APP 进程会话是否已经显示过开屏 */
    var splashShown: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MusicApp
            private set
    }
}