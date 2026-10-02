package com.example.music.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.music.R
import kotlinx.coroutines.delay

/**
 * 开屏界面。
 *
 * 图片资源：
 * - 竖屏用 `drawable/splash.jpg`（竖版开屏）
 * - 横屏由系统自动改用 `drawable-land/splash.jpg`（横版开屏）
 * 两张图都已经是整幅设计稿，所以直接铺满，淡入后进主界面。
 */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500))
        delay(1300)
        onFinish()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1026)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "焰火音乐",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value)
        )
    }
}
