package com.example.music.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.isActive

@Composable
fun CdCover(
    coverUrl: String?,
    songName: String,
    artistName: String,
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xF0101218),
                        Color(0xF8080910)
                    )
                )
            )
            .border(1.dp, Color(0x33FFFFFF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // ★ CD 几乎铺满整个圆
        RotatingCd(
            coverUrl = coverUrl,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize(0.98f)
        )

        // 中心：播放/暂停
        Box(
            Modifier
                .align(Alignment.Center)
                .size(66.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6B9FFF),
                            Color(0xFF8E5BFF)
                        )
                    )
                )
                .border(3.dp, Color(0xEEFFFFFF), CircleShape)
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying)
                    Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "播放/暂停",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // 上方：歌曲名
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color(0xF00A0B10))
                    .border(1.5.dp, Color(0x99FFFFFF), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = songName.ifBlank { "未播放" },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 下方：歌手
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color(0xF00A0B10))
                    .border(1.5.dp, Color(0x99FFFFFF), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = artistName.ifBlank { "未知歌手" },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 左侧：上一首
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xF00A0B10))
                .border(1.5.dp, Color(0x99FFFFFF), CircleShape)
                .clickable { onPrev() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // 右侧：下一首
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xF00A0B10))
                .border(1.5.dp, Color(0x99FFFFFF), CircleShape)
                .clickable { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "下一首",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun RotatingCd(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = 20000,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Box(
        modifier
            .graphicsLayer { rotationZ = rotation.value }
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF1A1D28),
                        Color(0xFF0A0B10),
                        Color(0xFF1A1D28),
                        Color(0xFF0A0B10),
                        Color(0xFF1A1D28)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 内圈封面（铺满，不留黑边）
        Box(
            Modifier
                .fillMaxSize(0.82f)
                .clip(CircleShape)
                .background(Color(0xFF1A1D28)),
            contentAlignment = Alignment.Center
        ) {
            if (!coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🎵", fontSize = 44.sp)
            }
        }

        // 中心黑胶孔
        Box(
            Modifier
                .fillMaxSize(0.20f)
                .clip(CircleShape)
                .background(Color(0xFF0A0B10))
        )
    }
}