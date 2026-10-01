package com.example.music.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerDial(
    speedKmh: Float,
    modifier: Modifier = Modifier
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
        Canvas(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)
            val startAngle = 135f
            val sweepAngle = 270f

            val arcRadius = radius * 0.90f
            val arcWidth = radius * 0.115f

            // 分段绘制：绿 → 黄 → 红
            val segments = 90
            val segSweep = sweepAngle / segments
            for (i in 0 until segments) {
                val fraction = i.toFloat() / (segments - 1)
                val color = speedGradient(fraction)
                drawArc(
                    color = color,
                    startAngle = startAngle + i * segSweep,
                    sweepAngle = segSweep + 1.2f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = arcWidth, cap = StrokeCap.Butt)
                )
            }

            // 刻度
            val tickCount = 19
            val tickOuter = radius * 0.82f
            val tickInner = radius * 0.72f
            for (i in 0 until tickCount) {
                val angleDeg = startAngle + (i.toFloat() / (tickCount - 1)) * sweepAngle
                val rad = Math.toRadians(angleDeg.toDouble())
                val outer = Offset(
                    center.x + cos(rad).toFloat() * tickOuter,
                    center.y + sin(rad).toFloat() * tickOuter
                )
                val inner = Offset(
                    center.x + cos(rad).toFloat() * tickInner,
                    center.y + sin(rad).toFloat() * tickInner
                )
                drawLine(
                    color = Color(0x99FFFFFF),
                    start = inner,
                    end = outer,
                    strokeWidth = radius * 0.014f
                )
            }

            // ★ 指针：颜色跟随所指向的速度区间
            val speedFraction = (speedKmh / 180f).coerceIn(0f, 1f)
            val needleAngle = startAngle + speedFraction * sweepAngle
            val rad = Math.toRadians(needleAngle.toDouble())
            val needleStart = Offset(
                center.x + cos(rad).toFloat() * radius * 0.38f,
                center.y + sin(rad).toFloat() * radius * 0.38f
            )
            val needleEnd = Offset(
                center.x + cos(rad).toFloat() * radius * 0.68f,
                center.y + sin(rad).toFloat() * radius * 0.68f
            )
            val needleColor = speedGradient(speedFraction)

            drawLine(
                color = needleColor,
                start = needleStart,
                end = needleEnd,
                strokeWidth = radius * 0.06f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = needleColor,
                radius = radius * 0.045f,
                center = needleStart
            )

            // 中心数字
            drawContext.canvas.nativeCanvas.apply {
                val numPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = radius * 0.62f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(20f, 0f, 0f, android.graphics.Color.parseColor("#6B9FFF"))
                }
                drawText(
                    "${speedKmh.toInt()}",
                    center.x,
                    center.y + radius * 0.22f,
                    numPaint
                )

                val unitPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(190, 255, 255, 255)
                    textSize = radius * 0.17f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(
                    "km/h",
                    center.x,
                    center.y + radius * 0.44f,
                    unitPaint
                )
            }
        }
    }
}

/**
 * 速度 → 颜色：
 *   0   → 绿
 *   90  → 黄
 *   180 → 红
 */
private fun speedGradient(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return if (f < 0.5f) {
        val t = f * 2f
        lerpColor(Color(0xFF00E676), Color(0xFFFFC107), t)
    } else {
        val t = (f - 0.5f) * 2f
        lerpColor(Color(0xFFFFC107), Color(0xFFFF1744), t)
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f
)