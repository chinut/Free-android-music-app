package com.example.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.music.player.PlayerHolder
import com.example.music.ui.MusicApp
import com.example.music.ui.theme.MusicTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    // ★ 保存外部分享进来的文本
    private val incomingShareFlow = MutableStateFlow<String?>(null)

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerHolder.init(applicationContext)

        // 处理冷启动时的 Intent
        incomingShareFlow.value = extractShareText(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val shareText by incomingShareFlow.collectAsState()
            MusicTheme {
                MusicApp(
                    initialShareText = shareText,
                    onShareConsumed = { incomingShareFlow.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 处理 APP 已经运行时收到的分享
        incomingShareFlow.value = extractShareText(intent)
    }

    private fun extractShareText(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        val type = intent.type ?: return null
        if (!type.startsWith("text/")) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }
}