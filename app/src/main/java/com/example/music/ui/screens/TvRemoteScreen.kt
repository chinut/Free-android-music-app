package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music.data.tv.TvRemoteDiscovery
import com.example.music.data.tv.TvRemotePrefs
import com.example.music.data.tv.YanhuoRemote
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Accent = Color(0xFF8EC5FF)
private val AccentSoft = Color(0x338EC5FF)
private val BtnBg = Color(0x26FFFFFF)
private val OkGreen = Color(0xFF7CE38B)

/**
 * 电视机网络遥控。
 *
 * 打开即用：先用上次成功的地址连接，连不上就自动扫描局域网，用户不需要填任何东西。
 * 界面就是一个干净的虚拟遥控器 —— 协议只传键码，所以这里无需理解电视各界的语义。
 */
@Composable
fun TvRemoteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { TvRemotePrefs(context) }

    // null=连接中，true=已连上，false=没找到
    var connected by remember { mutableStateOf<Boolean?>(null) }
    var client by remember { mutableStateOf<YanhuoRemote?>(null) }
    var textToSend by remember { mutableStateOf("") }
    var sentHint by remember { mutableStateOf("") }

    /** 连接流程：缓存地址 → 局域网扫描。 */
    suspend fun connect() {
        connected = null
        // 1) 先用上次成功的地址
        val cached = prefs.client()
        if (cached != null && cached.ping()) {
            client = cached
            connected = true
            return
        }
        // 2) 缓存失效或没配过：扫描局域网
        val found = TvRemoteDiscovery.discover(context)
        if (found != null) {
            prefs.host = found
            val c = prefs.client()
            if (c != null && c.ping()) {
                client = c
                connected = true
                return
            }
        }
        connected = false
    }

    LaunchedEffect(Unit) { connect() }

    // 连上后每 5 秒心跳；断了自动重连
    LaunchedEffect(connected) {
        if (connected != true) return@LaunchedEffect
        while (true) {
            delay(5000)
            val c = client ?: continue
            if (!c.ping()) {
                connect()
                return@LaunchedEffect
            }
        }
    }

    fun send(key: YanhuoRemote.Key) {
        val c = client ?: return
        scope.launch { c.key(key) }
    }

    fun sendText() {
        val c = client ?: return
        val t = textToSend
        if (t.isEmpty()) return
        scope.launch {
            val n = c.text(t)
            sentHint = if (n > 0) "已发送" else "发送失败"
            textToSend = ""
            delay(1500)
            sentHint = ""
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        // ---------- 极简顶栏：返回 + 连接状态 ----------
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (connected) {
                            true -> OkGreen
                            false -> Color(0xFFFF6B6B)
                            null -> Color(0x66FFFFFF)
                        }
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                when (connected) {
                    true -> "已连接电视"
                    false -> "没找到电视"
                    null -> "正在连接…"
                },
                color = Color(0xAAFFFFFF),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            when (connected) {
                null -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Accent,
                    strokeWidth = 2.dp
                )

                false -> Text(
                    "重试",
                    color = Accent,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentSoft)
                        .clickable { scope.launch { connect() } }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )

                true -> Unit
            }
        }

        // 只在没找到时给一句人话说明，平时完全不占地方
        if (connected == false) {
            Text(
                "请确认手机与电视在同一 WiFi，且电视已开启「允许手机调试」",
                color = Color(0x77FFFFFF),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
            )
        }

        // ---------- 方向键 + 确定 ----------
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RemoteKey(Icons.Default.KeyboardArrowUp, "上") { send(YanhuoRemote.Key.UP) }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RemoteKey(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "左") {
                    send(YanhuoRemote.Key.LEFT)
                }
                Spacer(Modifier.width(14.dp))
                Box(
                    Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(AccentSoft)
                        .clickable { send(YanhuoRemote.Key.OK) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("确定", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                RemoteKey(Icons.AutoMirrored.Filled.KeyboardArrowRight, "右") {
                    send(YanhuoRemote.Key.RIGHT)
                }
            }
            Spacer(Modifier.height(10.dp))
            RemoteKey(Icons.Default.KeyboardArrowDown, "下") { send(YanhuoRemote.Key.DOWN) }
        }

        // ---------- 返回 / 菜单 ----------
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WideKey("返回", Modifier.weight(1f)) { send(YanhuoRemote.Key.BACK) }
            WideKey("菜单", Modifier.weight(1f)) { send(YanhuoRemote.Key.MENU) }
        }
        Spacer(Modifier.height(10.dp))

        // ---------- 音量 ----------
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WideKey("音量 −", Modifier.weight(1f)) { send(YanhuoRemote.Key.VOL_DOWN) }
            WideKey("静音", Modifier.weight(1f)) { send(YanhuoRemote.Key.MUTE) }
            WideKey("音量 +", Modifier.weight(1f)) { send(YanhuoRemote.Key.VOL_UP) }
        }
        Spacer(Modifier.height(10.dp))

        // ---------- 播放控制 ----------
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WideKey("上一集", Modifier.weight(1f)) { send(YanhuoRemote.Key.PREV) }
            WideKey("播放/暂停", Modifier.weight(1f)) { send(YanhuoRemote.Key.PLAY_PAUSE) }
            WideKey("下一集", Modifier.weight(1f)) { send(YanhuoRemote.Key.NEXT) }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- 文字输入（电视上打字用） ----------
        OutlinedTextField(
            value = textToSend,
            onValueChange = { textToSend = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = connected == true,
            placeholder = { Text("输入文字发送到电视", color = Color(0x66FFFFFF), fontSize = 14.sp) },
            trailingIcon = {
                if (sentHint.isNotEmpty()) {
                    Text(sentHint, color = OkGreen, fontSize = 12.sp)
                } else {
                    IconButton(
                        enabled = textToSend.isNotEmpty() && connected == true,
                        onClick = { sendText() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (textToSend.isNotEmpty()) Accent else Color(0x44FFFFFF)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Color(0x33FFFFFF),
                disabledBorderColor = Color(0x22FFFFFF),
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendText() })
        )

        Spacer(Modifier.height(16.dp))
    }
}

/** 圆形方向键。 */
@Composable
private fun RemoteKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(BtnBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(38.dp))
    }
}

/** 宽条按键。 */
@Composable
private fun WideKey(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BtnBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 15.sp)
    }
}
