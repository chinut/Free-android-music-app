package com.example.music.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.core.content.ContextCompat
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
 * 连接策略（支持一个家庭有多台电视）：
 * 1. 先用**用户选定过**的那台，连上就直接进遥控器，不再打扰；
 * 2. 连不上或还没选过 → 扫描局域网：
 *    - 只找到 1 台 → 直接用
 *    - 找到多台 → 弹列表让用户挑（标题带设备名、副标题带 IP，可用「亮一下」定位）
 * 3. 断线自动重连；已连接时点标题栏的「切换」可改控制另一台。
 */
@Composable
fun TvRemoteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { TvRemotePrefs(context) }

    // null=连接中，true=已连上，false=没找到
    var connected by remember { mutableStateOf<Boolean?>(null) }
    var client by remember { mutableStateOf<YanhuoRemote?>(null) }
    var connectedHost by remember { mutableStateOf("") }
    var found by remember { mutableStateOf<List<TvRemoteDiscovery.Found>>(emptyList()) }
    var picking by remember { mutableStateOf(false) }
    /** 正在连的是哪台（选择列表里显示进度，避免点了没反应的感觉） */
    var connectingHost by remember { mutableStateOf<String?>(null) }
    var connectingPort by remember { mutableStateOf<Int?>(null) }
    /** 正在「响一声」的是哪台（key = 地址:端口） */
    var locatingKey by remember { mutableStateOf<String?>(null) }
    /** 「响一声」成功的那个 key，用来在列表里显示「就是这台」 */
    var locateResult by remember { mutableStateOf<String?>(null) }
    /** 选择失败的原因，显示在选择列表里 */
    var pickError by remember { mutableStateOf("") }
    var textToSend by remember { mutableStateOf("") }
    var sentHint by remember { mutableStateOf("") }
    /** 局域网权限是否已被拒绝（拒绝后要引导去系统设置） */
    var localNetDenied by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }

    // Android 16 / API 36 起，访问局域网是运行时权限。
    // 没有它，App 内的 socket 连不上电视 —— 表现为一直「没找到电视」。
    val needLocalNet = Build.VERSION.SDK_INT >= 36
    val localNetPermission = "android.permission.ACCESS_LOCAL_NETWORK"

    fun hasLocalNet(): Boolean =
        !needLocalNet ||
            ContextCompat.checkSelfPermission(context, localNetPermission) ==
            PackageManager.PERMISSION_GRANTED

    /** 选中某台电视并连上。 */
    suspend fun useTv(host: String, port: Int, byUser: Boolean) {
        connectingHost = host
        connectingPort = port
        pickError = ""
        val c = YanhuoRemote(host = host, port = port, token = prefs.token)
        val ok = c.ping()
        connectingHost = null
        connectingPort = null
        if (ok) {
            prefs.port = port
            prefs.select(host, byUser)
            client = c
            connectedHost = if (port == TvRemotePrefs.DEFAULT_PORT) host else "$host:$port"
            connected = true
            picking = false
        } else {
            // 留在选择列表并说明原因，用户可以直接改选另一台
            connected = false
            pickError = "连不上 $host:$port，可能电视关机或不在同一网络"
        }
    }

    /**
     * 连接电视。
     *
     * @param forcePick 点「切换」时为 true：跳过"连回上次那台"的捷径，直接弹出列表，
     *                  否则用户点了会毫无变化（又连回同一台）。
     */
    suspend fun connect(forcePick: Boolean = false) {
        connected = null
        picking = false
        found = emptyList()

        // 调试：`adb shell am start -n com.example.music/.MainActivity -e tvRemoteDemo 3`
        // 直接灌入假的电视列表，用来验证多台时的选择界面（只在显式传 extra 时生效）
        demoDevices(context)?.let { demo ->
            found = demo
            picking = true
            connected = false
            return
        }

        // 1) 用户选定过的那台优先，连上就直接进遥控器。
        //
        //    forcePick（点「切换」）时必须跳过这一步：否则会又连回当前这台，
        //    界面毫无变化，用户看到的就是"点切换没反应"。
        if (!forcePick && prefs.configured && prefs.userChosen) {
            val c = prefs.client()
            if (c != null && c.ping()) {
                client = c
                connectedHost = if (prefs.port == TvRemotePrefs.DEFAULT_PORT) {
                    prefs.host
                } else {
                    "${prefs.host}:${prefs.port}"
                }
                connected = true
                return
            }
        }

        // 2) 扫描局域网，把**所有**电视都找出来
        //    边扫边显示：家里电视多、端口段也要扫，整体耗时会比只扫 8899 长
        val list = TvRemoteDiscovery.discoverAll(context, prefs.port) { partial ->
            found = partial
            // 多点几台就出来：边扫边显示，不用干等
            if (partial.size > 1 && (!prefs.userChosen || forcePick)) picking = true
        }
        found = list
        when {
            list.isEmpty() -> connected = false

            // 只有一台且不是来切换的：直接用，无需打扰用户
            list.size == 1 && !forcePick -> useTv(list[0].host, list[0].port, byUser = false)

            // 点「切换」时即使只有一台也把列表显示出来，
            // 让用户看到确实只有这一台，而不是"点了没反应"
            else -> {
                // 多台：之前选过的那台若还在，继续用它（切换时除外）
                val saved = TvRemoteDiscovery.findSaved(list, prefs.host, prefs.port)
                val savedOk = if (!forcePick && prefs.userChosen && saved != null) {
                    YanhuoRemote(host = saved.host, port = saved.port, token = prefs.token).ping()
                } else false

                if (savedOk && saved != null) {
                    client = YanhuoRemote(host = saved.host, port = saved.port, token = prefs.token)
                    connectedHost = saved.subtitle
                    connected = true
                    picking = false
                } else {
                    // 没选过、或选过的那台不在了、或用户主动要切换 → 让用户挑
                    picking = true
                    connected = false
                }
            }
        }
    }

    // 局域网权限的请求器（必须定义在 connect() 之后，回调里要调它）
    val localNetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionAsked = true
        localNetDenied = !granted
        // 不论结果都继续：授权了就正常扫描，没授权会扫不到，界面已有对应提示
        scope.launch { connect() }
    }

    LaunchedEffect(Unit) {
        // 先要局域网权限，拿到（或被拒）之后再扫描
        if (!hasLocalNet() && !permissionAsked) {
            localNetLauncher.launch(localNetPermission)
        } else {
            connect()
        }
    }

    // 连上后每 5 秒心跳；断了自动重连
    LaunchedEffect(connected, connectedHost) {
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

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            RemoteHeader(
                connected = connected,
                picking = picking,
                connectedHost = connectedHost,
                onBack = onBack,
                onRetry = { scope.launch { connect(forcePick = true) } },
                onSwitch = { scope.launch { connect(forcePick = true) } },
            )

            if (connected == false && !picking) {
                Text(
                    if (localNetDenied) {
                        "需要「访问本地网络」权限才能搜索电视，请到系统设置里允许"
                    } else {
                        "请确认手机与电视在同一 WiFi，且电视已开启「允许手机调试」"
                    },
                    color = if (localNetDenied) Color(0xFFFF9A9A) else Color(0x77FFFFFF),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
                )
            }

            RemotePad(
                enabled = connected == true && !picking,
                onSend = { send(it) },
            )

            RemoteMediaKeys(
                enabled = connected == true && !picking,
                onSend = { send(it) },
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = textToSend,
                onValueChange = { textToSend = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = connected == true && !picking,
                placeholder = { Text("输入文字发送到电视", color = Color(0x66FFFFFF), fontSize = 14.sp) },
                trailingIcon = {
                    if (sentHint.isNotEmpty()) {
                        Text(sentHint, color = OkGreen, fontSize = 12.sp)
                    } else {
                        IconButton(
                            enabled = textToSend.isNotEmpty() && connected == true && !picking,
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

        // ---------- 多台电视时让用户挑 ----------
        if (picking) {
            TvPickerOverlay(
                devices = found,
                connectingHost = connectingHost,
                connectingPort = connectingPort,
                locatingKey = locatingKey,
                locateResult = locateResult,
                error = pickError,
                onPick = { scope.launch { useTv(it.host, it.port, byUser = true) } },
                onRescan = { scope.launch { connect(forcePick = true) } },
                onLocate = { d ->
                    val k = "${d.host}:${d.port}"
                    locatingKey = k
                    locateResult = null
                    scope.launch {
                        // 「响一声」：把音量 +1 再 −1，净效果为 0，但电视会弹出音量条，
                        // 用户一看哪台有反应就知道是哪台。不能改其它状态。
                        val c = YanhuoRemote(host = d.host, port = d.port, token = prefs.token)
                        val ok = c.volumeUp() != null
                        if (ok) delay(220)
                        c.volumeDown()
                        locatingKey = null
                        if (ok) {
                            locateResult = k
                            delay(6000)
                            if (locateResult == k) locateResult = null
                        } else {
                            pickError = "${d.subtitle} 没响应，可能已关机或不在同一网络"
                        }
                    }
                },
            )
        }
    }
}

// ==================== 调试 ====================

/**
 * 调试用：从启动 Intent 的 `tvRemoteDemo` extra 构造假的电视列表。
 *
 * ```
 * # 只验证界面（虚构地址，连不上）
 * adb shell am start -n com.example.music/.MainActivity -e tvRemoteDemo 3
 *
 * # 用真实可达的地址，可以真连上、真按键（支持 IP 或 IP:端口，逗号分隔）
 * adb shell am start -n com.example.music/.MainActivity -e tvRemoteDemo 2 \
 *     -e tvRemoteHosts "10.0.2.2:8899,10.0.2.2:8900"
 * ```
 *
 * - `tvRemoteDemo`：台数（0 或未传表示不启用）
 * - `tvRemoteHosts`：可选，逗号分隔；支持 `IP` 或 `IP:端口`
 */
private fun demoDevices(context: android.content.Context): List<TvRemoteDiscovery.Found>? {
    val activity = context as? android.app.Activity ?: return null
    val n = activity.intent?.getStringExtra("tvRemoteDemo")?.toIntOrNull() ?: return null
    if (n <= 0) return null

    val count = n.coerceAtMost(4)
    val custom = activity.intent?.getStringExtra("tvRemoteHosts")
        ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }

    // 默认用虚构地址（只验证界面）；给了 tvRemoteHosts 就用真地址，可以真正连上
    val specs = custom ?: (1..count).map { "192.168.1.${10 * it}" }

    // 故意用同一个设备名，模拟「家里几台电视都叫焰火TV」的真实情况：
    // 名称无法区分时，只能靠 IP/端口 和「亮一下」来辨认
    return specs.take(count).map { spec ->
        val host = spec.substringBeforeLast(':', spec)
        val port = spec.substringAfterLast(':', "")
            .toIntOrNull() ?: TvRemotePrefs.DEFAULT_PORT
        TvRemoteDiscovery.Found(
            host = host,
            app = "焰火TV",
            protocol = 1,
            screen = "home",
            port = port,
        )
    }
}

// ==================== 顶栏 ====================

@Composable
private fun RemoteHeader(
    connected: Boolean?,
    picking: Boolean,
    connectedHost: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSwitch: () -> Unit,
) {
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
                    when {
                        picking -> Accent
                        connected == true -> OkGreen
                        connected == false -> Color(0xFFFF6B6B)
                        else -> Color(0x66FFFFFF)
                    }
                )
        )
        Spacer(Modifier.width(8.dp))

        val label = when {
            picking -> "选择要控制的电视"
            connected == true -> if (connectedHost.isNotBlank()) "已连接 · $connectedHost" else "已连接"
            connected == false -> "没找到电视"
            else -> "正在连接…"
        }
        Text(
            label,
            color = Color(0xAAFFFFFF),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        when {
            picking -> Unit

            connected == null -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Accent,
                strokeWidth = 2.dp
            )

            connected == false -> Text(
                "重试",
                color = Accent,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentSoft)
                    .clickable { onRetry() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )

            // 已连接时提供「切换电视」，多台电视的家庭用得上
            else -> Text(
                "切换",
                color = Accent,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentSoft)
                    .clickable { onSwitch() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

// ==================== 选择电视 ====================

@Composable
private fun TvPickerOverlay(
    devices: List<TvRemoteDiscovery.Found>,
    connectingHost: String?,
    connectingPort: Int?,
    locatingKey: String?,
    locateResult: String?,
    error: String,
    onPick: (TvRemoteDiscovery.Found) -> Unit,
    onRescan: () -> Unit,
    onLocate: (TvRemoteDiscovery.Found) -> Unit,
) {
    // 同一地址上可能有两台电视（端口不同），所以必须用 地址+端口 一起做身份
    fun keyOf(d: TvRemoteDiscovery.Found) = "${d.host}:${d.port}"

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xF21A1533))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "找到 ${devices.size} 台电视",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            // 名字能不能区分要看电视端自报什么：协议里的 app 字段如果都是「焰火TV」，
            // 就只能靠地址区分。据实提示，别把话说死。
            val namesDistinct = devices.map { it.title }.distinct().size == devices.size
            Text(
                if (namesDistinct) {
                    "按名字或下面的地址选一台控制。拿不准是哪台时，点「响一声」——" +
                        "被点的那台会放一下音量，你看哪台有反应就是它。"
                } else {
                    "这几台电视自报的名字相同（协议里没有设备名），只能按下面的地址区分。" +
                        "拿不准哪台是哪台时，点「响一声」——被点的那台会放一下音量，" +
                        "你看哪台有反应就是它。"
                },
                color = Color(0x88FFFFFF),
                fontSize = 12.sp
            )

            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFFF9A9A), fontSize = 12.sp)
            }

            devices.forEach { d ->
                val k = keyOf(d)
                val rowBusy = connectingHost == d.host && (connectingPort == null || connectingPort == d.port)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (locateResult == k) Color(0x337CE38B) else BtnBg)
                        .clickable(enabled = connectingHost == null && locatingKey == null) { onPick(d) }
                        .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(d.title, color = Color.White, fontSize = 15.sp)
                        Text(d.subtitle, color = Color(0x88FFFFFF), fontSize = 12.sp)
                    }

                    when {
                        rowBusy -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Accent,
                            strokeWidth = 2.dp
                        )

                        locatingKey == k -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Accent,
                            strokeWidth = 2.dp
                        )

                        // 刚点过「响一声」并成功 → 明确告诉用户就是这台
                        locateResult == k -> Text(
                            "✓ 就是这台",
                            color = OkGreen,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> Text(
                            "响一声",
                            color = Accent,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentSoft)
                                .clickable(enabled = connectingHost == null && locatingKey == null) {
                                    onLocate(d)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "重新扫描",
                    color = Accent,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = locatingKey == null) { onRescan() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ==================== 按键区 ====================

@Composable
private fun ColumnScope.RemotePad(enabled: Boolean, onSend: (YanhuoRemote.Key) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RemoteKey(Icons.Default.KeyboardArrowUp, "上", enabled) { onSend(YanhuoRemote.Key.UP) }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RemoteKey(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft, "左", enabled
            ) { onSend(YanhuoRemote.Key.LEFT) }
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(AccentSoft)
                    .clickable(enabled = enabled) { onSend(YanhuoRemote.Key.OK) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "确定",
                    color = if (enabled) Accent else Color(0x66FFFFFF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))
            RemoteKey(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, "右", enabled
            ) { onSend(YanhuoRemote.Key.RIGHT) }
        }
        Spacer(Modifier.height(10.dp))
        RemoteKey(Icons.Default.KeyboardArrowDown, "下", enabled) { onSend(YanhuoRemote.Key.DOWN) }
    }
}

@Composable
private fun RemoteMediaKeys(enabled: Boolean, onSend: (YanhuoRemote.Key) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WideKey("返回", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.BACK) }
        WideKey("菜单", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.MENU) }
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WideKey("音量 −", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.VOL_DOWN) }
        WideKey("静音", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.MUTE) }
        WideKey("音量 +", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.VOL_UP) }
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WideKey("上一集", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.PREV) }
        WideKey("播放/暂停", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.PLAY_PAUSE) }
        WideKey("下一集", enabled, Modifier.weight(1f)) { onSend(YanhuoRemote.Key.NEXT) }
    }
}

/** 圆形方向键。 */
@Composable
private fun RemoteKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(BtnBg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = desc,
            tint = if (enabled) Color.White else Color(0x55FFFFFF),
            modifier = Modifier.size(38.dp)
        )
    }
}

/** 宽条按键。 */
@Composable
private fun WideKey(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BtnBg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) Color.White else Color(0x55FFFFFF), fontSize = 15.sp)
    }
}
