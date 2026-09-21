package com.example.music.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.music.player.PlayerHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 默认渐变色（无封面时用）
private val DefaultColors = listOf(
    Color(0xFF1A1240),
    Color(0xFF10132A),
    Color(0xFF0A0B10)
)

/**
 * 动态渐变背景。
 * 跟随当前播放歌曲的封面颜色，自动生成渐变。
 * 没有播放或没有封面时，使用默认深蓝紫渐变。
 */
@Composable
fun DynamicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 抓成本地变量，避免 smart cast 问题
    val coverState by PlayerHolder.coverUrl.collectAsState()
    val cover: String? = coverState

    var target by remember { mutableStateOf(DefaultColors) }

    LaunchedEffect(cover) {
        target = if (cover.isNullOrEmpty()) {
            DefaultColors
        } else {
            extractColors(context, cover) ?: DefaultColors
        }
    }

    // 颜色平滑过渡
    val c0 by animateColorAsState(target[0], tween(800), label = "c0")
    val c1 by animateColorAsState(target[1], tween(800), label = "c1")
    val c2 by animateColorAsState(target[2], tween(800), label = "c2")

    Box(modifier.fillMaxSize()) {

        // 1) 封面模糊底
        if (!cover.isNullOrEmpty()) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.35f
                        scaleX = 2f
                        scaleY = 2f
                    }
            )
        }

        // 2) 动态渐变叠加
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            c0.copy(alpha = 0.75f),
                            c1.copy(alpha = 0.82f),
                            c2.copy(alpha = 0.9f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1080f, 1920f)
                    )
                )
        )

        // 3) 径向暗角，增强层次
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x55000000),
                            Color(0x99000000)
                        ),
                        center = Offset(540f, 900f),
                        radius = 1600f
                    )
                )
        )

        content()
    }
}

// ============================================================
// 从封面提取颜色
// ============================================================

private suspend fun extractColors(context: Context, url: String): List<Color>? =
    withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .size(200)
                .build()

            val result = context.imageLoader.execute(request)
            if (result !is SuccessResult) return@withContext null

            val bitmap = drawableToBitmap(result.drawable) ?: return@withContext null

            val palette = Palette.from(bitmap).generate()

            val vibrant = palette.getVibrantColor(0xFF3A2A6A.toInt())
            val darkVibrant = palette.getDarkVibrantColor(0xFF1F1B3A.toInt())
            val muted = palette.getMutedColor(0xFF181228.toInt())

            listOf(
                darken(vibrant, 0.55f),
                darken(darkVibrant, 0.65f),
                darken(muted, 0.5f)
            )
        } catch (e: Exception) {
            null
        }
    }

private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap? {
    if (drawable is BitmapDrawable) return drawable.bitmap
    val w = drawable.intrinsicWidth.coerceAtLeast(1)
    val h = drawable.intrinsicHeight.coerceAtLeast(1)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bmp
}

private fun darken(color: Int, factor: Float): Color {
    val c = Color(color)
    return Color(
        red = (c.red * factor).coerceIn(0f, 1f),
        green = (c.green * factor).coerceIn(0f, 1f),
        blue = (c.blue * factor).coerceIn(0f, 1f),
        alpha = 1f
    )
}