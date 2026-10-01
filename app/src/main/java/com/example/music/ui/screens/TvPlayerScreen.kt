package com.example.music.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import com.example.music.data.tv.TvCategory
import com.example.music.data.tv.TvChannel
import com.example.music.data.tv.TvFavorites
import com.example.music.data.tv.TvQualityOption
import com.example.music.data.tv.TvStreamUrl
import com.example.music.data.tv.TvWebBridge
import com.example.music.data.tv.TvWebViewClient
import com.example.music.data.tv.parseQualityOptions
import kotlinx.coroutines.delay

/** 工具条 / 浮层自动隐藏的时间。 */
private const val AUTO_HIDE_MS = 5000L

/** 网页类频道：页面加载完之后，最多等多久要看到视频真的出画面，否则换源。 */
private const val PAGE_VIDEO_TIMEOUT_MS = 30_000L

/**
 * 探测页面里的视频是否已经真正开始播放。
 * 返回 "1" 表示有 video 元素且已缓冲到可播放、并且在播。
 */
private const val PROBE_VIDEO_JS = """
(function(){
  try{
    var vs=document.querySelectorAll('video');
    for(var i=0;i<vs.length;i++){
      var v=vs[i];
      if(v && !v.paused && v.readyState>=2 && (v.videoWidth>0 || v.currentTime>0)){ return '1'; }
    }
    return '0';
  }catch(e){ return '0'; }
})();
"""

/**
 * 在线电视播放页。
 *
 * 播放路线分两种，由频道地址决定：
 * - **直连流**（m3u8/flv，含土拨鼠包装页里的内层地址）→ 原生 ExoPlayer，
 *   可自定义 Referer，解决防盗链与无扩展名 m3u8 的问题；
 * - **站点网页**（央视、各省台直播页）→ WebView + tv-web 注入脚本。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TvPlayerScreen(
    initialChannel: TvChannel,
    allCategories: List<TvCategory>,
    /** 形如「源 2/3」，换过源时显示在标题旁。 */
    sourceInfo: String? = null,
    /** 该频道是否还有备用源（决定是否启用自动换源与看门狗）。 */
    hasAlternates: Boolean = false,
    /** 所有同名源都失败，此时才展示错误面板。 */
    allSourcesFailed: Boolean = false,
    /**
     * 当前源播放失败时回调。返回 true 表示已自动切到备用源（本页会继续加载新地址），
     * false 表示所有源都试完了，此时才向用户报错。
     */
    onSourceFailed: () -> Boolean = { false },
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var channel by remember { mutableStateOf(initialChannel) }
    var showChannelDrawer by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var qualities by remember { mutableStateOf<List<TvQualityOption>>(emptyList()) }
    var currentQualityId by remember { mutableStateOf<String?>(null) }
    val favUrls by TvFavorites.flow.collectAsState()
    val favorite = favUrls.contains(channel.url)

    // 直连流走原生播放器；null 表示要用 WebView 加载站点网页
    val nativeStream = remember(channel.url) { TvStreamUrl.directMediaOf(channel.url) }
    val useNative = nativeStream != null

    @Suppress("UNUSED_VARIABLE")
    var nativePlayer: ExoPlayer? by remember { mutableStateOf(null) }

    // 原生播放器自己的全屏状态
    var nativeFullscreen by remember { mutableStateOf(false) }

    // 「重载」/ 换源用：变化时重建原生播放器
    var nativeRetry by remember { mutableStateOf(0) }

    // 防止同一个源失败后反复触发换源
    var sourceFailed by remember { mutableStateOf(false) }

    // 网页是否已加载完（用于判断"页面开了但视频起不来"）
    var pageLoaded by remember { mutableStateOf(false) }

    // 视频是否真的开始出画面。注意不能用 loading：它 8 秒后会被兜底清掉
    var videoStarted by remember { mutableStateOf(false) }

    // 自动换源后，外部会把 initialChannel 换成备用源，这里同步到本地状态触发重新加载
    LaunchedEffect(initialChannel.url) {
        if (initialChannel.url != channel.url) {
            channel = initialChannel
            qualities = emptyList()
            currentQualityId = null
            errorText = null
            loading = true
            sourceFailed = false
            pageLoaded = false
            videoStarted = false
            nativeRetry++
        }
    }

    // 播放时的方向策略：
    // - 全屏（网页全屏 / 原生全屏）→ 锁横屏
    // - 普通播放 → FULL_SENSOR：完全跟随重力传感器，不看系统「自动旋转」开关
    //   （视频类应用的通行做法，YouTube/B站都是这样）。
    //   用它的另一个好处：应用方向始终与设备一致，系统就不会弹那个「旋转建议」按钮。
    // - 离开播放页 → 恢复 UNSPECIFIED，避免把方向偏好带给主页。
    DisposableEffect(fullscreenView, nativeFullscreen) {
        val inFullscreen = fullscreenView != null || nativeFullscreen
        activity?.requestedOrientation = if (inFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // 播放电视时暂停音乐，避免两路声音
    LaunchedEffect(Unit) {
        runCatching { com.example.music.player.PlayerHolder.controller?.pause() }
        TvFavorites.urls(context)
    }

    // 自动隐藏工具条
    LaunchedEffect(controlsVisible, showChannelDrawer, showQualitySheet) {
        if (controlsVisible && !showChannelDrawer && !showQualitySheet) {
            delay(AUTO_HIDE_MS)
            controlsVisible = false
        }
    }

    // 加载提示兜底
    LaunchedEffect(loading) {
        if (loading) {
            delay(8000)
            loading = false
        }
    }

    // ---------- 失败处理与切台（定义在前面，供下面的 effect / 播放器引用） ----------

    /** 当前源播放失败：先请求换源，只有所有源都试完才向用户报错。 */
    fun handleSourceFailure(reason: String) {
        loading = false
        if (sourceFailed) return
        sourceFailed = true
        val switched = runCatching { onSourceFailed() }.getOrDefault(false)
        if (!switched) {
            errorText = reason
        }
        // 换源成功时 initialChannel 会变，上面的 LaunchedEffect 会重新加载
    }

    /** 是否还有备用源可换（决定要不要启用"没画面就换源"的看门狗）。 */
    fun onSourceFailedAvailable(): Boolean = hasAlternates

    // ==================== WebView 路线 ====================
    val webView = remember {
        TapAwareWebView(
            context,
            onTap = {
                loading = false
                controlsVisible = !controlsVisible
            },
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(AndroidColor.BLACK)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                @Suppress("DEPRECATION")
                databaseEnabled = true
                allowFileAccess = true
                mediaPlaybackRequiresUserGesture = false
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // 用桌面版 UA：移动 UA 会被央视频等站点拦成「前往客户端」提示页，
                // 土拨鼠（utao）用的也是这个桌面 UA。
                userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            }
        }
    }

    val fullscreenContainer = remember {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(AndroidColor.BLACK)
            visibility = View.GONE
        }
    }

    val client = remember { TvWebViewClient(context, liveMode = true) }

    // 网页类频道加载失败（域名解析失败 / 404 / 连接被拒）时也走换源
    DisposableEffect(client) {
        client.onPageError = { desc -> handleSourceFailure("加载失败：$desc") }
        client.onPageLoaded = { pageLoaded = true }
        onDispose {
            client.onPageError = null
            client.onPageLoaded = null
        }
    }

    // 「页面加载完了，但视频始终起不来」也要换源。
    // 央视频那类站点就是这种情况：页面 200、有 <video>，但画面永远不出来。
    //
    // 重要：只有「还有备用源可换」时才启用这个看门狗。
    // 否则单源频道会因为加载慢而被误判成失败，反而给用户弹一个假的错误。
    LaunchedEffect(channel.url, useNative, pageLoaded) {
        if (useNative || !pageLoaded || videoStarted || sourceFailed) return@LaunchedEffect
        if (!onSourceFailedAvailable()) return@LaunchedEffect
        var waited = 0L
        while (waited < PAGE_VIDEO_TIMEOUT_MS) {
            delay(1000)
            waited += 1000
            if (videoStarted || sourceFailed) return@LaunchedEffect
            var probed = false
            runCatching {
                webView.evaluateJavascript(PROBE_VIDEO_JS) { v ->
                    probed = true
                    if (v != null && v != "null" && v.contains("1")) videoStarted = true
                }
            }
            if (!probed) return@LaunchedEffect
        }
        if (!videoStarted && !sourceFailed) {
            handleSourceFailure("该源没有画面")
        }
    }
    val bridge = remember {
        TvWebBridge(context) { event ->
            when (event) {
                is TvWebBridge.TvEvent.VideoQuality -> qualities = parseQualityOptions(event.json)
                is TvWebBridge.TvEvent.ExitPlayer -> onClose()
                else -> Unit
            }
        }
    }

    DisposableEffect(Unit) {
        webView.apply {
            webViewClient = client
            addJavascriptInterface(bridge, "_api")
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress >= 80) loading = false
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    val v = view ?: return
                    (v.parent as? ViewGroup)?.removeView(v)
                    fullscreenContainer.removeAllViews()
                    fullscreenContainer.addView(v)
                    fullscreenContainer.visibility = View.VISIBLE
                    fullscreenView = v
                }

                override fun onHideCustomView() {
                    fullscreenContainer.removeAllViews()
                    fullscreenContainer.visibility = View.GONE
                    fullscreenView = null
                }

                override fun onConsoleMessage(msg: ConsoleMessage?): Boolean = true
            }
        }
        onDispose {
            runCatching {
                webView.removeJavascriptInterface("_api")
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.destroy()
            }
        }
    }

    /** 切台：只换地址，不重建 WebView，也不退回列表。 */
    fun tuneTo(target: TvChannel) {
        if (target.url == channel.url) {
            showChannelDrawer = false
            return
        }
        channel = target
        TvFavorites.rememberLast(context, target.url)
        loading = true
        errorText = null
        qualities = emptyList()
        currentQualityId = null
        showChannelDrawer = false
        showQualitySheet = false
        controlsVisible = false
        sourceFailed = false
    }

    // 频道变化 → 按需加载相应播放器
    LaunchedEffect(channel.url, useNative) {
        TvFavorites.rememberLast(context, channel.url)
        if (!useNative) {
            // 每个站点需要的 UA 不同，加载前先切好
            webView.settings.userAgentString = TvStreamUrl.userAgentFor(channel.url)
            webView.loadUrl(channel.url)
        }
    }

    BackHandler {
        when {
            nativeFullscreen -> nativeFullscreen = false
            fullscreenView != null -> runCatching {
                webView.evaluateJavascript(
                    "document.exitFullscreen&&document.exitFullscreen()", null
                )
            }

            showQualitySheet -> showQualitySheet = false
            showChannelDrawer -> showChannelDrawer = false
            else -> onClose()
        }
    }

    fun selectQuality(option: TvQualityOption) {
        currentQualityId = option.id
        showQualitySheet = false
        val js = "try{var e=document.getElementById('${option.id}');" +
            "if(e){e.click();}" +
            "else{var x=document.getElementById('xhz-${option.id}');if(x)x.click();}}catch(err){}"
        runCatching { webView.evaluateJavascript(js, null) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 播放区：直连流用原生播放器，站点网页用 WebView
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (nativeStream != null) {
                NativeStreamPlayer(
                    streamUrl = nativeStream,
                    retryKey = nativeRetry,
                    modifier = Modifier.fillMaxSize(),
                    onReady = {
                        loading = false
                        videoStarted = true
                    },
                    onError = { code -> handleSourceFailure("播放失败（$code）") },
                    playerRef = { nativePlayer = it },
                )
            } else {
                AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
                AndroidView(factory = { fullscreenContainer }, modifier = Modifier.fillMaxSize())
            }
        }

        // ---------- 顶部工具条（自动隐藏；原生全屏时隐藏） ----------
        AnimatedVisibility(
            visible = controlsVisible && !nativeFullscreen,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(TvPalette.Toolbar)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                    )
                }
                Text(
                    channel.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // 换过源时提示当前用的是第几个源
                sourceInfo?.let { info ->
                    Spacer(Modifier.width(6.dp))
                    Text(
                        info,
                        color = TvPalette.Accent,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TvPalette.AccentSoft)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.weight(1f))

                if (useNative) {
                    IconButton(onClick = { nativeFullscreen = true }) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "全屏",
                            tint = Color.White,
                        )
                    }
                }

                IconButton(onClick = { TvFavorites.toggle(context, channel.url) }) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (favorite) "取消收藏" else "收藏",
                        tint = if (favorite) TvPalette.AccentPink else Color.White,
                    )
                }

                if (qualities.isNotEmpty()) {
                    IconButton(onClick = { showQualitySheet = true }) {
                        Icon(Icons.Default.Hd, contentDescription = "清晰度", tint = Color.White)
                    }
                }

                IconButton(onClick = { showChannelDrawer = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = "频道",
                        tint = Color.White,
                    )
                }

                IconButton(onClick = {
                    loading = true
                    errorText = null
                    if (useNative) {
                        nativeRetry++
                    } else {
                        webView.reload()
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "重载", tint = Color.White)
                }
            }
        }

        // 原生全屏时的退出按钮
        if (nativeFullscreen) {
            IconButton(
                onClick = { nativeFullscreen = false },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "退出全屏",
                    tint = Color.White,
                )
            }
        }

        // ---------- 加载提示 ----------
        if (loading && errorText == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        loading = false
                        controlsVisible = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TvPalette.Accent)
                    Spacer(Modifier.height(12.dp))
                    Text("正在加载 ${channel.name} …", color = Color(0xCCFFFFFF), fontSize = 13.sp)
                }
            }
        }

        // ---------- 失败提示 ----------
        errorText?.let { msg ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(TvPalette.Scrim)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("无法播放「${channel.name}」", color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(msg, color = Color(0x99FFFFFF), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (allSourcesFailed && (sourceInfo != null)) {
                            "已试过该频道的所有源，都播不了"
                        } else {
                            "该频道源可能已失效或受地域限制"
                        },
                        color = Color(0x88FFFFFF),
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 还有备用源没试完时，才给「换个源试试」入口
                        if (hasAlternates) {
                            Text(
                                "换个源试试",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(TvPalette.AccentSoft)
                                    .clickable {
                                        errorText = null
                                        loading = true
                                        sourceFailed = false
                                        runCatching { onSourceFailed() }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        Text(
                            "换个频道",
                            color = TvPalette.Accent,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(TvPalette.AccentSoft)
                                .clickable { showChannelDrawer = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        // ---------- 换台抽屉 ----------
        AnimatedVisibility(
            visible = showChannelDrawer,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ChannelDrawer(
                categories = allCategories,
                currentUrl = channel.url,
                onPick = { tuneTo(it) },
                onDismiss = { showChannelDrawer = false },
            )
        }

        // ---------- 清晰度 ----------
        AnimatedVisibility(
            visible = showQualitySheet,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            QualitySheet(
                options = qualities,
                selectedId = currentQualityId,
                onPick = { selectQuality(it) },
                onDismiss = { showQualitySheet = false },
            )
        }
    }
}

// ==================== 换台抽屉 ====================

@Composable
private fun ChannelDrawer(
    categories: List<TvCategory>,
    currentUrl: String,
    onPick: (TvChannel) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val favUrls by TvFavorites.flow.collectAsState()
    val initialIndex = remember {
        categories.indexOfFirst { cat -> cat.channels.any { it.url == currentUrl } }
            .coerceAtLeast(0)
    }
    var categoryIndex by remember { mutableStateOf(initialIndex) }
    var favOnly by remember { mutableStateOf(false) }

    val channels = remember(categoryIndex, favOnly, favUrls) {
        if (favOnly) {
            categories.flatMap { it.channels }.filter { favUrls.contains(it.url) }
        } else {
            categories.getOrNull(categoryIndex)?.channels.orEmpty()
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(categoryIndex, favOnly) {
        val idx = channels.indexOfFirst { it.url == currentUrl }
        if (idx > 0) listState.scrollToItem(idx)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(TvPalette.Scrim)
            .clickable(onClick = onDismiss)
    ) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(TvPalette.Sheet)
                .clickable(enabled = false) {}
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("换台", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(
                    "当前：${channels.size} 个频道",
                    color = Color(0x88FFFFFF),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DrawerChip("★ 收藏", favOnly) { favOnly = true }
                }
                items(categories.size) { i ->
                    val cat = categories[i]
                    DrawerChip("${cat.name} ${cat.channels.size}", !favOnly && i == categoryIndex) {
                        favOnly = false
                        categoryIndex = i
                    }
                }
            }

            if (channels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "还没有收藏频道，点播放页的心形收藏",
                        color = Color(0x88FFFFFF),
                        fontSize = 13.sp,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(channels, key = { it.url + it.name }) { ch ->
                        ChannelDrawerRow(
                            channel = ch,
                            playing = ch.url == currentUrl,
                            favorite = favUrls.contains(ch.url),
                            onClick = { onPick(ch) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) TvPalette.AccentSoft else TvPalette.Chip)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) TvPalette.Accent else Color.Transparent,
                shape = RoundedCornerShape(15.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            color = if (selected) TvPalette.Accent else Color(0xCCFFFFFF),
            fontSize = 12.5.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChannelDrawerRow(
    channel: TvChannel,
    playing: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (playing) TvPalette.AccentSoft else TvPalette.Row)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (playing) "▶" else "  ", color = TvPalette.Playing, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            channel.name,
            color = if (playing) TvPalette.Accent else Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (favorite) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = TvPalette.AccentPink,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

// ==================== 清晰度面板 ====================

@Composable
private fun QualitySheet(
    options: List<TvQualityOption>,
    selectedId: String?,
    onPick: (TvQualityOption) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(TvPalette.Scrim)
            .clickable(onClick = onDismiss)
    ) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(TvPalette.Sheet)
                .clickable(enabled = false) {}
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 12.dp),
        ) {
            Text(
                "清晰度",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
            if (options.isEmpty()) {
                Text(
                    "该频道暂不支持切换清晰度",
                    color = Color(0x88FFFFFF),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                )
            } else {
                options.sortedByDescending { it.level }.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option) }
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            option.name,
                            color = if (option.id == selectedId) TvPalette.Accent else Color.White,
                            fontSize = 14.5.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (option.id == selectedId) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = TvPalette.Accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 工具 ====================

/** 与 App 主题一致的配色（跟随 MusicTheme 的蓝紫色调）。 */
private object TvPalette {
    val Accent = Color(0xFF8EC5FF)
    val AccentPink = Color(0xFFFF6B9F)
    val AccentSoft = Color(0x338EC5FF)
    val Playing = Color(0xFF7CE38B)
    val Toolbar = Color(0xCC1A1533)
    val Sheet = Color(0xF21A1533)
    val Chip = Color(0x22FFFFFF)
    val Row = Color(0x14FFFFFF)
    val Scrim = Color(0xB3000000)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * WebView 会消费所有触摸事件，父级 Compose 的 tap 检测收不到，
 * 所以在这里用 GestureDetector 识别单击来切换工具条，同时不干扰页面本身的滚动和点击。
 */
private class TapAwareWebView(
    context: Context,
    private val onTap: () -> Unit,
) : WebView(context) {

    private val detector = android.view.GestureDetector(
        context,
        object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: android.view.MotionEvent): Boolean = true
            override fun onSingleTapUp(e: android.view.MotionEvent): Boolean {
                onTap()
                return false
            }
        },
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        runCatching { detector.onTouchEvent(event) }
        return super.onTouchEvent(event)
    }
}
