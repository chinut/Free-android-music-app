package com.example.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.music.data.update.UpdateChecker
import com.example.music.data.update.UpdateInfo
import kotlinx.coroutines.launch

private val AccentBlue = Color(0xFF6B9FFF)
private val AccentPurple = Color(0xFF8E5BFF)

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentSource by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!downloading) onDismiss() }) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xF01A1240), Color(0xF00A0B10))
                    )
                )
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                // 标题
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AccentBlue, AccentPurple))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "发现新版本",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "v${info.versionName}",
                            color = Color(0x99FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 更新日志
                if (info.releaseNotes.isNotBlank()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x11FFFFFF))
                            .padding(12.dp)
                    ) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                info.releaseNotes,
                                color = Color(0xCCFFFFFF),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // 下载源提示
                val availableSources = info.apkSources.joinToString(" / ") { it.name }
                Text(
                    "可用源：$availableSources",
                    color = Color(0x77FFFFFF),
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(10.dp))

                // 下载进度 / 错误
                if (downloading) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = AccentBlue,
                        trackColor = Color(0x22FFFFFF)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "正在从 $currentSource 下载… ${(progress * 100).toInt()}%",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(10.dp))
                } else if (errorMsg != null) {
                    Text(
                        errorMsg ?: "",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // 按钮行
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !downloading) { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("稍后", color = Color(0x99FFFFFF), fontSize = 14.sp)
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (downloading) {
                                    Brush.linearGradient(
                                        listOf(Color(0x33FFFFFF), Color(0x33FFFFFF))
                                    )
                                } else {
                                    Brush.linearGradient(listOf(AccentBlue, AccentPurple))
                                }
                            )
                            .clickable(enabled = !downloading) {
                                scope.launch {
                                    downloading = true
                                    errorMsg = null
                                    progress = 0f
                                    currentSource = info.primarySource

                                    val file = UpdateChecker.downloadApk(
                                        context = context,
                                        info = info,
                                        onProgress = { p -> progress = p },
                                        onSourceChange = { name -> currentSource = name },
                                    )
                                    downloading = false

                                    if (file != null) {
                                        try {
                                            UpdateChecker.installApk(context, file)
                                            onDismiss()
                                        } catch (e: Exception) {
                                            errorMsg = "安装失败：${e.message}"
                                        }
                                    } else {
                                        errorMsg = "所有源下载失败，请稍后再试"
                                    }
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (downloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            "立即更新",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}