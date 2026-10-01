package com.example.music.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.music.data.CruiseManager
import com.example.music.player.PlayerHolder
import com.example.music.ui.components.AmapMapView
import com.example.music.ui.components.CdCover
import com.example.music.ui.components.SpeedometerDial
import kotlin.math.abs

@Composable
fun CruiseScreen(
    onExit: () -> Unit,
    vm: CruiseViewModel = viewModel(),
) {
    val context = LocalContext.current

    val speed by vm.speedKmh.collectAsState()
    val bearing by vm.phoneBearing.collectAsState()
    val lat by vm.latitude.collectAsState()
    val lng by vm.longitude.collectAsState()

    val song by PlayerHolder.currentSong.collectAsState()
    val cover by PlayerHolder.coverUrl.collectAsState()
    val playing by PlayerHolder.isPlaying.collectAsState()

    // 强制横屏 + 屏幕常亮
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        CruiseManager.start(context)
    }

    DisposableEffect(Unit) {
        onDispose { CruiseManager.stop() }
    }

    BackHandler { onExit() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AmapMapView(
            latitude = lat,
            longitude = lng,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x00000000),
                            Color(0x33000000),
                            Color(0x77000000)
                        ),
                        radius = 1400f
                    )
                )
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val circleSize: Dp = minOf(maxWidth * 0.34f, maxHeight * 0.58f)
            val circleYOffset: Dp = 30.dp

            // 罗盘（背景 0x80 ≈ 50%）
            CompassStrip(
                bearing = bearing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.46f)
                    .height(36.dp)
            )

            // ★ 码表：透明度改为 0.5f，与罗盘一致
            SpeedometerDial(
                speedKmh = speed,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .offset(y = circleYOffset)
                    .size(circleSize)
                    .alpha(0.5f)
            )

            // ★ CD 播放器：透明度改为 0.5f，与罗盘一致
            CdCover(
                coverUrl = cover,
                songName = song?.name ?: "未播放",
                artistName = song?.artist ?: "",
                isPlaying = playing,
                onPrev = { PlayerHolder.prev() },
                onPlayPause = { PlayerHolder.toggle() },
                onNext = { PlayerHolder.next() },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .offset(y = circleYOffset)
                    .size(circleSize)
                    .alpha(0.5f)
            )
        }

        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x880A0B10))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Close, "退出巡航", tint = Color.White)
        }
    }
}

@Composable
private fun CompassStrip(
    bearing: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x80101218), Color(0x80080910))
                )
            )
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(50))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val bottomPad = h * 0.12f
            val usable = h - bottomPad * 2
            val halfW = w / 2 - 16f

            val normalizedBearing = ((bearing % 360) + 360) % 360

            var tickAngle = 0
            while (tickAngle < 360) {
                var rel = tickAngle.toFloat() - normalizedBearing
                while (rel > 180f) rel -= 360f
                while (rel < -180f) rel += 360f

                if (abs(rel) <= 90f) {
                    val x = cx + (rel / 90f) * halfW
                    val tickH = usable * 0.55f

                    drawLine(
                        color = Color(0xCCFFFFFF),
                        start = Offset(x, bottomPad + usable),
                        end = Offset(x, bottomPad + usable - tickH),
                        strokeWidth = 2.5f
                    )

                    val label = when (tickAngle) {
                        0 -> "N"
                        45 -> "NE"
                        90 -> "E"
                        135 -> "SE"
                        180 -> "S"
                        225 -> "SW"
                        270 -> "W"
                        315 -> "NW"
                        else -> ""
                    }
                    if (label.isNotEmpty()) {
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            x,
                            bottomPad + usable - tickH - 6f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = h * 0.42f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                        )
                    }
                }
                tickAngle += 45
            }

            val triY = bottomPad + usable + 2f
            val path = Path().apply {
                moveTo(cx, triY + 16f)
                lineTo(cx - 20f, triY)
                lineTo(cx + 20f, triY)
                close()
            }
            drawPath(path, color = Color(0xFF6B9FFF))
        }
    }
}