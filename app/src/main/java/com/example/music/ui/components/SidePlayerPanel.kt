package com.example.music.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music.data.PlaylistRepository
import com.example.music.player.PlayerHolder
import com.example.music.ui.screens.AddToPlaylistDialog
import com.example.music.ui.screens.RemoveFromPlaylistDialog

@Composable
fun SidePlayerPanel(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 240.dp,
    onExpand: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { PlaylistRepository(context) }
    val favoritedKeys by repo.allFavoritedKeys().collectAsState(initial = emptySet())

    val song by PlayerHolder.currentSong.collectAsState()
    val playing by PlayerHolder.isPlaying.collectAsState()
    val position by PlayerHolder.position.collectAsState()
    val duration by PlayerHolder.duration.collectAsState()
    val cover by PlayerHolder.coverUrl.collectAsState()

    val s = song ?: return
    val isFav = "${s.source}:${s.id}" in favoritedKeys

    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddToPlaylistDialog(song = s, onDismiss = { showAddDialog = false })
    }
    if (showRemoveDialog) {
        RemoveFromPlaylistDialog(
            song = s,
            onDismiss = { showRemoveDialog = false },
            onRemoved = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
        )
    }

    Column(
        modifier
            .width(width)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99000000),
                        Color(0xCC000000)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 封面：点击进全屏
        Box(
            Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x33FFFFFF))
                .clickable { onExpand() },
            contentAlignment = Alignment.Center
        ) {
            if (!cover.isNullOrEmpty()) {
                AsyncImage(
                    model = cover,
                    contentDescription = "点击打开全屏播放",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🎵", fontSize = 42.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = s.name.ifBlank { "未知歌曲" },
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = s.artist.ifBlank { "未知歌手" },
            color = Color(0x99FFFFFF),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        // ★ 可拖动进度条
        DraggableProgressBar(
            position = position,
            duration = duration,
            onSeek = { PlayerHolder.seekTo(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        // 时间
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(fmt(position), color = Color(0x88FFFFFF), fontSize = 11.sp)
            Text(fmt(duration), color = Color(0x88FFFFFF), fontSize = 11.sp)
        }

        Spacer(Modifier.height(14.dp))

        // 控制按钮
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isFav) showRemoveDialog = true else showAddDialog = true
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFav) "已收藏" else "收藏",
                    tint = if (isFav) Color(0xFFFF4D6D) else Color(0xCCFFFFFF),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { PlayerHolder.prev() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6B9FFF), Color(0xFF8E5BFF))
                        )
                    )
                    .clickable { PlayerHolder.toggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "播放/暂停",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(
                onClick = { PlayerHolder.next() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 可拖动进度条。
 */
@Composable
private fun DraggableProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var barWidth by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val realProgress = if (duration > 0L)
        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    else 0f
    val displayProgress = if (dragging) dragFraction else realProgress

    Box(
        modifier
            .height(20.dp)
            .onSizeChanged { barWidth = it.width.toFloat() }
            .pointerInput(duration) {
                detectTapGestures { offset ->
                    if (barWidth > 0f && duration > 0L) {
                        val f = (offset.x / barWidth).coerceIn(0f, 1f)
                        onSeek((f * duration).toLong())
                    }
                }
            }
            .pointerInput(duration) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        if (barWidth > 0f && duration > 0L) {
                            dragging = true
                            dragFraction = (offset.x / barWidth).coerceIn(0f, 1f)
                        }
                    },
                    onDragEnd = {
                        if (duration > 0L) {
                            onSeek((dragFraction * duration).toLong())
                        }
                        dragging = false
                    },
                    onDragCancel = { dragging = false },
                    onHorizontalDrag = { change, _ ->
                        if (barWidth > 0f) {
                            dragFraction = (change.position.x / barWidth).coerceIn(0f, 1f)
                        }
                        change.consume()
                    }
                )
            }
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // 底轨
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0x33FFFFFF))
            )
            // 已播
            Box(
                Modifier
                    .fillMaxWidth(displayProgress)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6B9FFF), Color(0xFF8E5BFF))
                        )
                    )
            )
            // 拖动时的小圆点
            if (dragging && barWidth > 0f) {
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                x = (displayProgress * barWidth - 5.dp.toPx()).toInt(),
                                y = 0
                            )
                        }
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private fun fmt(ms: Long): String {
    if (ms <= 0) return "00:00"
    val total = ms / 1000
    return "%02d:%02d".format(total / 60, total % 60)
}