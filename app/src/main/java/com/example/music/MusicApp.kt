package com.example.music

import android.app.Application

class MusicApp : Application() {

    /**
     * 本次 APP 进程会话是否已经触发过"自动播放"检查。
     */
    var autoPlayTriggered: Boolean = false

    /**
     * ★ 本次 APP 进程会话是否已经显示过开屏。
     * 屏幕旋转、切 Tab 都不会重置，只有杀掉 APP 重开才会重新显示。
     */
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