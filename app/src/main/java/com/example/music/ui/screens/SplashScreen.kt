package com.example.music.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.music.R
import kotlinx.coroutines.delay

/**
 * 开屏界面。
 * 居中显示 splash 图片，淡入 + 轻微放大，停留 1.2 秒后自动进入主界面。
 */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        // 淡入 + 放大
        alpha.animateTo(1f, tween(600))
        scale.animateTo(1f, tween(600))
        // 停留
        delay(1200)
        onFinish()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1240),
                        Color(0xFF10132A),
                        Color(0xFF0A0B10)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1080f, 1920f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "开屏",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(340.dp)
                .alpha(alpha.value)
                .scale(scale.value)
        )
    }
}