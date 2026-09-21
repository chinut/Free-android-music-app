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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.music.data.PlaylistRepository
import com.example.music.player.PlayerHolder
import com.example.music.ui.screens.AddToPlaylistDialog
import com.example.music.ui.screens.RemoveFromPlaylistDialog

@Composable
fun MiniPlayer(
    onExpand: () -> Unit,
    compact: Boolean = false
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

    val onFavClick = { if (isFav) showRemoveDialog = true else showAddDialog = true }

    Column(Modifier.fillMaxWidth()) {

        // ★ 可拖动的进度条
        DraggableProgressBar(
            position = position,
            duration = duration,
            onSeek = { PlayerHolder.seekTo(it) }
        )

        if (compact) {
            // ============================================================
            // 横屏紧凑版
            // ============================================================
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onExpand() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!cover.isNullOrEmpty()) {
                        AsyncImage(
                            model = cover,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("🎵")
                    }
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "${s.name} · ${s.artist}",
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onFavClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFav) "已收藏" else "收藏",
                        tint = if (isFav) Color(0xFFFF4D6D) else Color(0xCCFFFFFF),
                        modifier = Modifier.size(20.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = { PlayerHolder.toggle() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "播放/暂停",
                        tint = Color(0xFF8EC5FF),
                        modifier = Modifier.size(24.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onExpand,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ExpandLess,
                        contentDescription = "展开全屏",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            // ============================================================
            // 竖屏完整版
            // ============================================================
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable { onExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!cover.isNullOrEmpty()) {
                        AsyncImage(
                            model = cover,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("🎵")
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = s.name.ifBlank { "未知歌曲" },
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = s.artist.ifBlank { "未知歌手" },
                        color = Color(0x99FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onFavClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFav) "已收藏" else "收藏",
                        tint = if (isFav) Color(0xFFFF4D6D) else Color(0xCCFFFFFF),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = { PlayerHolder.prev() }) {
                    Icon(Icons.Default.SkipPrevious, "上一首", tint = Color.White)
                }
                IconButton(onClick = { PlayerHolder.toggle() }) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "播放/暂停",
                        tint = Color(0xFF8EC5FF)
                    )
                }
                IconButton(onClick = { PlayerHolder.next() }) {
                    Icon(Icons.Default.SkipNext, "下一首", tint = Color.White)
                }
            }
        }
    }
}

/**
 * 可拖动的迷你进度条。
 * 触摸热区 14dp，视觉只有 2dp，视觉和触感兼顾。
 */
@Composable
private fun DraggableProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
) {
    var barWidth by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val realProgress = if (duration > 0L)
        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    else 0f
    val displayProgress = if (dragging) dragFraction else realProgress

    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
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
            Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0x22FFFFFF))
            )
            Box(
                Modifier
                    .fillMaxWidth(displayProgress)
                    .height(2.dp)
                    .background(Color(0xFF8EC5FF))
            )
        }
    }
}