package com.example.music.ui.screens

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.music.data.UserPreferencesStore
import com.example.music.data.api.ApiHolder
import com.example.music.data.cache.AudioCache
import com.example.music.data.update.UpdateChecker
import com.example.music.data.update.UpdateInfo
import com.example.music.player.PlayerHolder
import com.example.music.ui.components.ImportShareDialog
import com.example.music.ui.components.UpdateDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// 统一的图标颜色
private val IconBlue = Color(0xFF8EC5FF)

@Composable
fun SettingsScreen(
    onReopenOnboarding: () -> Unit = {},
    onOpenPreferenceEdit: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferencesStore(context) }

    var cacheSize by remember { mutableStateOf(0L) }
    var refreshing by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }

    var autoPlayEnabled by remember { mutableStateOf(prefs.isAutoPlayEnabled()) }
    var keepScreenOnEnabled by remember { mutableStateOf(prefs.isKeepScreenOnEnabled()) }

    // ★ 更新弹窗状态
    var pendingUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }

    val versionName = remember {
        try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

    suspend fun refresh() {
        refreshing = true
        cacheSize = withContext(Dispatchers.IO) { AudioCache.totalCacheBytes(context) }
        refreshing = false
    }

    // ★ 检查更新
    fun checkUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        scope.launch {
            try {
                val pInfo = context.packageManager
                    .getPackageInfo(context.packageName, 0)
                val currentCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode
                }
                val info = UpdateChecker.checkForUpdate(currentCode)
                if (info != null) {
                    pendingUpdate = info
                } else {
                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "检查失败：${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                checkingUpdate = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    if (!isLandscape) {
        // ============================================================
        // 竖屏：单列
        // ============================================================
        Column(Modifier.fillMaxSize()) {
            Text(
                "⚙️ 设置",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {
                item {
                    SettingsItems(
                        autoPlayEnabled = autoPlayEnabled,
                        onAutoPlayChange = {
                            autoPlayEnabled = it
                            prefs.setAutoPlayEnabled(it)
                        },
                        keepScreenOnEnabled = keepScreenOnEnabled,
                        onKeepScreenOnChange = {
                            keepScreenOnEnabled = it
                            prefs.setKeepScreenOnEnabled(it)
                        },
                        onReopenOnboarding = onReopenOnboarding,
                        onOpenPreferenceEdit = onOpenPreferenceEdit,
                        onImportShare = { showImport = true },
                        cacheSize = cacheSize,
                        refreshing = refreshing,
                        onClearCache = { showConfirm = true },
                        onRefresh = { scope.launch { refresh() } },
                        onCheckUpdate = { checkUpdate() },
                        checkingUpdate = checkingUpdate,
                        versionName = versionName
                    )
                }
            }
        }
    } else {
        // ============================================================
        // 横屏：两列网格
        // ============================================================
        Column(Modifier.fillMaxSize()) {
            Text(
                "⚙️ 设置",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    ListItem(
                        headlineContent = { Text("启动后自动播放", color = Color.White) },
                        supportingContent = {
                            Text("进入 APP 时自动播放推荐的第一首", color = Color(0x99FFFFFF))
                        },
                        leadingContent = {
                            Icon(Icons.Default.PlayCircle, null, tint = IconBlue)
                        },
                        trailingContent = {
                            Switch(
                                checked = autoPlayEnabled,
                                onCheckedChange = {
                                    autoPlayEnabled = it
                                    prefs.setAutoPlayEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0A0B10),
                                    checkedTrackColor = IconBlue,
                                    uncheckedThumbColor = Color(0x99FFFFFF),
                                    uncheckedTrackColor = Color(0x33FFFFFF)
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    ListItem(
                        headlineContent = { Text("全屏播放时屏幕常亮", color = Color.White) },
                        supportingContent = {
                            Text("全屏歌词界面不自动熄屏", color = Color(0x99FFFFFF))
                        },
                        leadingContent = {
                            Icon(Icons.Default.LightMode, null, tint = IconBlue)
                        },
                        trailingContent = {
                            Switch(
                                checked = keepScreenOnEnabled,
                                onCheckedChange = {
                                    keepScreenOnEnabled = it
                                    prefs.setKeepScreenOnEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0A0B10),
                                    checkedTrackColor = IconBlue,
                                    uncheckedThumbColor = Color(0x99FFFFFF),
                                    uncheckedTrackColor = Color(0x33FFFFFF)
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))
                }

                item {
                    ListItem(
                        headlineContent = { Text("重新选择音乐偏好", color = Color.White) },
                        supportingContent = {
                            Text("清空重选", color = Color(0x99FFFFFF))
                        },
                        leadingContent = {
                            Icon(Icons.Default.Tune, null, tint = IconBlue)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onReopenOnboarding() }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("编辑偏好 / 黑名单", color = Color.White) },
                        supportingContent = {
                            Text("增减喜欢的歌手和风格，屏蔽不喜欢的", color = Color(0x99FFFFFF))
                        },
                        leadingContent = {
                            Icon(Icons.Default.Block, null, tint = IconBlue)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onOpenPreferenceEdit() }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("导入分享码", color = Color.White) },
                        supportingContent = {
                            Text("好友分享的歌单", color = Color(0x99FFFFFF))
                        },
                        leadingContent = {
                            Icon(Icons.Default.Download, null, tint = IconBlue)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showImport = true }
                    )
                }

                // ★ 检查更新
                item {
                    ListItem(
                        headlineContent = { Text("检查更新", color = Color.White) },
                        supportingContent = {
                            Text(
                                if (checkingUpdate) "检查中…"
                                else "从 GitHub 获取最新版本",
                                color = Color(0x99FFFFFF)
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.SystemUpdate, null, tint = IconBlue)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable(enabled = !checkingUpdate) { checkUpdate() }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))
                }

                item {
                    ListItem(
                        headlineContent = { Text("缓存占用", color = Color.White) },
                        supportingContent = {
                            Text(
                                if (refreshing) "计算中…"
                                else formatSize(cacheSize),
                                color = Color(0x99FFFFFF)
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.CleaningServices, null, tint = IconBlue)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { showConfirm = true }) { Text("清空缓存", maxLines = 1) }
                        OutlinedButton(onClick = {
                            if (!refreshing) scope.launch { refresh() }
                        }) { Text("刷新") }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))
                }

                item {
                    ListItem(
                        headlineContent = { Text("下载目录", color = Color.White) },
                        supportingContent = {
                            Text("音乐 / Yinyueku", color = Color(0x99FFFFFF))
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("后台播放", color = Color.White) },
                        supportingContent = {
                            Text("支持耳机线控", color = Color(0x99FFFFFF))
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    ListItem(
                        headlineContent = { Text("关于", color = Color.White) },
                        supportingContent = {
                            Column {
                                Text("游戏东西群内专供", color = Color(0xCCFFFFFF))
                                Text(
                                    "版本 $versionName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x77FFFFFF)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }

    if (showImport) {
        ImportShareDialog(
            onDismiss = { showImport = false },
            onImported = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        )
    }

    // ★ 更新弹窗
    pendingUpdate?.let { info ->
        UpdateDialog(info = info, onDismiss = { pendingUpdate = null })
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("清空缓存") },
            text = { Text("将删除：\n· 音频缓存\n· 歌词缓存\n· 封面缓存\n\n正在播放的歌曲会暂停。") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    scope.launch {
                        PlayerHolder.stop()
                        withContext(Dispatchers.IO) {
                            AudioCache.clear()
                            ApiHolder.infoCacheDir?.deleteRecursively()
                            File(context.filesDir, "artwork_cache").deleteRecursively()
                            File(context.filesDir, "song_info_cache").deleteRecursively()
                        }
                        refreshing = true
                        cacheSize = withContext(Dispatchers.IO) {
                            AudioCache.totalCacheBytes(context)
                        }
                        refreshing = false
                        Toast.makeText(context, "缓存已清空", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("确定清空") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
}

/**
 * 竖屏用的连续设置项
 */
@Composable
private fun SettingsItems(
    autoPlayEnabled: Boolean,
    onAutoPlayChange: (Boolean) -> Unit,
    keepScreenOnEnabled: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onReopenOnboarding: () -> Unit,
    onOpenPreferenceEdit: () -> Unit,
    onImportShare: () -> Unit,
    cacheSize: Long,
    refreshing: Boolean,
    onClearCache: () -> Unit,
    onRefresh: () -> Unit,
    onCheckUpdate: () -> Unit,
    checkingUpdate: Boolean,
    versionName: String,
) {
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color(0xFF0A0B10),
        checkedTrackColor = IconBlue,
        uncheckedThumbColor = Color(0x99FFFFFF),
        uncheckedTrackColor = Color(0x33FFFFFF)
    )

    Column {
        ListItem(
            headlineContent = { Text("启动后自动播放", color = Color.White) },
            supportingContent = {
                Text("进入 APP 时自动播放推荐列表的第一首可播放歌曲", color = Color(0x99FFFFFF))
            },
            leadingContent = {
                Icon(Icons.Default.PlayCircle, null, tint = IconBlue)
            },
            trailingContent = {
                Switch(
                    checked = autoPlayEnabled,
                    onCheckedChange = onAutoPlayChange,
                    colors = switchColors
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        ListItem(
            headlineContent = { Text("全屏播放时屏幕常亮", color = Color.White) },
            supportingContent = {
                Text("全屏歌词界面不自动熄屏", color = Color(0x99FFFFFF))
            },
            leadingContent = {
                Icon(Icons.Default.LightMode, null, tint = IconBlue)
            },
            trailingContent = {
                Switch(
                    checked = keepScreenOnEnabled,
                    onCheckedChange = onKeepScreenOnChange,
                    colors = switchColors
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))

        ListItem(
            headlineContent = { Text("重新选择音乐偏好", color = Color.White) },
            supportingContent = {
                Text("清空重选", color = Color(0x99FFFFFF))
            },
            leadingContent = {
                Icon(Icons.Default.Tune, null, tint = IconBlue)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onReopenOnboarding() }
        )

        ListItem(
            headlineContent = { Text("编辑偏好 / 黑名单", color = Color.White) },
            supportingContent = {
                Text("增减喜欢的歌手和风格，屏蔽不喜欢的", color = Color(0x99FFFFFF))
            },
            leadingContent = {
                Icon(Icons.Default.Block, null, tint = IconBlue)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onOpenPreferenceEdit() }
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))

        ListItem(
            headlineContent = { Text("导入分享码", color = Color.White) },
            supportingContent = {
                Text("粘贴好友发来的歌单或单曲分享码", color = Color(0x99FFFFFF))
            },
            leadingContent = {
                Icon(Icons.Default.Download, null, tint = IconBlue)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onImportShare() }
        )

        // ★ 检查更新
        ListItem(
            headlineContent = { Text("检查更新", color = Color.White) },
            supportingContent = {
                Text(
                    if (checkingUpdate) "检查中…"
                    else "从 GitHub 获取最新版本",
                    color = Color(0x99FFFFFF)
                )
            },
            leadingContent = {
                Icon(Icons.Default.SystemUpdate, null, tint = IconBlue)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable(enabled = !checkingUpdate) { onCheckUpdate() }
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))

        ListItem(
            headlineContent = { Text("缓存占用", color = Color.White) },
            supportingContent = {
                Text(
                    if (refreshing) "计算中…" else "已缓存 ${formatSize(cacheSize)}",
                    color = Color(0x99FFFFFF)
                )
            },
            leadingContent = {
                Icon(Icons.Default.CleaningServices, null, tint = IconBlue)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onClearCache) { Text("清空缓存") }
            OutlinedButton(onClick = onRefresh) { Text("刷新") }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))

        ListItem(
            headlineContent = { Text("下载目录", color = Color.White) },
            supportingContent = {
                Text("MP3 会保存到系统音乐库：音乐 / Yinyueku", color = Color(0x99FFFFFF))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
        ListItem(
            headlineContent = { Text("数据源", color = Color.White) },
            supportingContent = {
                Text("yinyueku.cn", color = Color(0x99FFFFFF))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
        ListItem(
            headlineContent = { Text("关于", color = Color.White) },
            supportingContent = {
                Column {
                    Text("此程序仅用于个人学习使用", color = Color(0xCCFFFFFF))
                    Text(
                        "版本 $versionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x77FFFFFF)
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> "%.2f GB".format(bytes / 1024.0 / 1024 / 1024)
    bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024)
    bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}