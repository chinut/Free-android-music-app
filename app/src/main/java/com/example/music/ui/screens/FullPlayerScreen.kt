// ============================================================
// ui/screens/FullPlayerScreen.kt —— 横竖屏自适应 + 屏幕常亮 + 桃心收藏
// ============================================================
package com.example.music.ui.screens

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music.data.LrcLine
import com.example.music.data.PlaylistRepository
import com.example.music.data.Song
import com.example.music.data.UserPreferencesStore
import com.example.music.player.PlayMode
import com.example.music.player.PlayerHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

private val AccentBlue = Color(0xFF6B9FFF)
private val AccentPurple = Color(0xFF8E5BFF)
private val BgDeep = Color(0xFF0A0B10)
private val FavRed = Color(0xFFFF4D6D)

@Composable
fun FullPlayerScreen(onClose: () -> Unit) {
    val context = LocalContext.current

    // 屏幕常亮
    val prefs = remember { UserPreferencesStore(context) }
    val keepScreenOn = prefs.isKeepScreenOnEnabled()

    if (keepScreenOn) {
        DisposableEffect(Unit) {
            val activity = context as? Activity
            val window = activity?.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    BackHandler(enabled = true) { onClose() }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val songState by PlayerHolder.currentSong.collectAsState()
    val song: Song? = songState
    val playing by PlayerHolder.isPlaying.collectAsState()
    val position by PlayerHolder.position.collectAsState()
    val duration by PlayerHolder.duration.collectAsState()
    val mode by PlayerHolder.playMode.collectAsState()
    val lyric by PlayerHolder.lyric.collectAsState()
    val cover by PlayerHolder.coverUrl.collectAsState()

    // 收藏状态
    val repo = remember { PlaylistRepository(context) }
    val favoritedKeys by repo.allFavoritedKeys().collectAsState(initial = emptySet())
    val isFav = song?.let { "${it.source}:${it.id}" in favoritedKeys } ?: false

    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    song?.let { s ->
        if (showAddDialog) {
            AddToPlaylistDialog(song = s, onDismiss = { showAddDialog = false })
        }
        if (showRemoveDialog) {
            RemoveFromPlaylistDialog(
                song = s,
                onDismiss = { showRemoveDialog = false },
                onRemoved = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    var showClose by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        showClose = false
    }

    val onFavClick = {
        if (isFav) showRemoveDialog = true else showAddDialog = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BgDeep)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showClose = !showClose }
    ) {
        BackgroundLayer(cover)

        if (isLandscape) {
            // ============================================================
            // 横屏
            // ============================================================
            Row(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    RotatingCover(cover = cover, playing = playing, size = 160.dp)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = song?.name ?: "未播放",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    val artistText = song?.artist.orEmpty().ifBlank { "请选择一首歌曲" }
                    val albumText = song?.album.orEmpty()
                    Text(
                        text = if (albumText.isNotBlank()) "$artistText · $albumText" else artistText,
                        color = Color(0x99FFFFFF),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    // 进度条 + 播放模式
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            ProgressBar(
                                position = position,
                                duration = duration,
                                onSeek = { PlayerHolder.seekTo(it) }
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { PlayerHolder.cyclePlayMode() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    PlayMode.SINGLE_LOOP -> Icons.Default.RepeatOne
                                    PlayMode.SHUFFLE -> Icons.Default.Shuffle
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = mode.label,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 控制栏：桃心 + prev + play + next
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onFavClick,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFav) "已收藏" else "收藏",
                                tint = if (isFav) FavRed else Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = { PlayerHolder.prev() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "上一首",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Box(
                            Modifier
                                .size(64.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    ambientColor = AccentBlue,
                                    spotColor = AccentPurple
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(colors = listOf(AccentBlue, AccentPurple))
                                )
                                .clickable { PlayerHolder.toggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "播放/暂停",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        IconButton(
                            onClick = { PlayerHolder.next() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "下一首",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    LyricView(
                        lyric = lyric,
                        position = position,
                        modifier = Modifier.fillMaxSize(),
                        large = true
                    )
                }
            }
        } else {
            // ============================================================
            // 竖屏
            // ============================================================
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(52.dp))

                RotatingCover(cover = cover, playing = playing, size = 260.dp)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = song?.name ?: "未播放",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                val artistText = song?.artist.orEmpty().ifBlank { "请选择一首歌曲" }
                val albumText = song?.album.orEmpty()
                Text(
                    text = if (albumText.isNotBlank()) "$artistText · $albumText" else artistText,
                    color = Color(0x99FFFFFF),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                LyricView(
                    lyric = lyric,
                    position = position,
                    modifier = Modifier.weight(1f),
                    large = false
                )

                Spacer(Modifier.height(12.dp))

                ProgressBar(
                    position = position,
                    duration = duration,
                    onSeek = { PlayerHolder.seekTo(it) }
                )

                Spacer(Modifier.height(16.dp))

                // 控制栏：mode + prev + play + next + 桃心
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { PlayerHolder.cyclePlayMode() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                PlayMode.SINGLE_LOOP -> Icons.Default.RepeatOne
                                PlayMode.SHUFFLE -> Icons.Default.Shuffle
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = mode.label,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { PlayerHolder.prev() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "上一首",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Box(
                        Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                ambientColor = AccentBlue,
                                spotColor = AccentPurple
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = listOf(AccentBlue, AccentPurple))
                            )
                            .clickable { PlayerHolder.toggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "播放/暂停",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = { PlayerHolder.next() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "下一首",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // ★ 桃心
                    IconButton(
                        onClick = onFavClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFav) "已收藏" else "收藏",
                            tint = if (isFav) FavRed else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // 右上角关闭按钮
        androidx.compose.animation.AnimatedVisibility(
            visible = showClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            ) {
                Icon(Icons.Default.ExpandMore, "关闭", tint = Color.White)
            }
        }
    }
}

// ============================================================
// 背景层
// ============================================================
@Composable
private fun BackgroundLayer(cover: String?) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A1240),
                            Color(0xFF10132A),
                            BgDeep
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1080f, 1920f)
                    )
                )
        )

        if (!cover.isNullOrEmpty()) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.28f
                        scaleX = 1.8f
                        scaleY = 1.8f
                    }
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x00000000),
                            Color(0xCC0A0B10),
                            Color(0xFF0A0B10)
                        ),
                        center = Offset(540f, 900f),
                        radius = 1400f
                    )
                )
        )
    }
}

// ============================================================
// 旋转封面
// ============================================================
@Composable
private fun RotatingCover(
    cover: String?,
    playing: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(playing) {
        if (playing) {
            while (isActive) {
                rotationAnim.animateTo(
                    targetValue = rotationAnim.value + 360f,
                    animationSpec = tween(durationMillis = 24000, easing = LinearEasing)
                )
            }
        } else {
            rotationAnim.stop()
        }
    }

    Box(
        Modifier
            .size(size)
            .shadow(
                elevation = 24.dp,
                shape = CircleShape,
                ambientColor = AccentBlue,
                spotColor = AccentPurple
            )
            .clip(CircleShape)
            .graphicsLayer { rotationZ = rotationAnim.value },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            AccentBlue,
                            AccentPurple,
                            Color(0xFFFF6B9F),
                            AccentBlue
                        )
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1D2E)),
            contentAlignment = Alignment.Center
        ) {
            if (!cover.isNullOrEmpty()) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text("🎵", fontSize = 56.sp)
            }
        }

        Box(
            Modifier
                .size(size * 0.25f)
                .clip(CircleShape)
                .background(Color(0xCC0A0B10))
        )
        Box(
            Modifier
                .size(size * 0.06f)
                .clip(CircleShape)
                .background(Color(0xFF2A2E42))
        )
    }
}

// ============================================================
// 歌词
// ============================================================
@Composable
private fun LyricView(
    lyric: List<LrcLine>,
    position: Long,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val listState = rememberLazyListState()

    val currentIndex = remember(lyric, position) {
        if (lyric.isEmpty()) -1
        else lyric.indexOfLast { it.time <= position }.coerceAtLeast(0)
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            runCatching { listState.animateScrollToItem(currentIndex) }
        }
    }

    if (lyric.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "♪ 暂无歌词",
                color = Color(0x55FFFFFF),
                fontSize = if (large) 18.sp else 15.sp
            )
        }
        return
    }

    Box(modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = if (large) 100.dp else 80.dp)
        ) {
            itemsIndexed(lyric) { i, line ->
                val active = i == currentIndex
                val scale = if (active) 1f else 0.92f
                val alpha = if (active) 1f else 0.5f

                Text(
                    text = line.text,
                    color = if (active) Color.White else Color(0x99FFFFFF),
                    fontSize = when {
                        active && large -> 22.sp
                        active -> 18.sp
                        large -> 16.sp
                        else -> 14.sp
                    },
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { PlayerHolder.seekTo(line.time) }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .padding(
                            vertical = if (large) 12.dp else 10.dp,
                            horizontal = 16.dp
                        )
                )
            }
        }
    }
}

// ============================================================
// 进度条
// ============================================================
@Composable
private fun ProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    var barWidth by remember { mutableFloatStateOf(0f) }

    val total = duration.coerceAtLeast(1L).toFloat()
    val value = if (dragging) dragValue else position.toFloat().coerceIn(0f, total)
    val progress = if (total > 0f) (value / total).coerceIn(0f, 1f) else 0f

    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .onSizeChanged { barWidth = it.width.toFloat() }
                .pointerInput(total) {
                    detectTapGestures { offset ->
                        if (barWidth > 0f && total > 0f) {
                            val p = (offset.x / barWidth).coerceIn(0f, 1f)
                            onSeek((p * total).toLong())
                        }
                    }
                }
                .pointerInput(total) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragging = true
                            if (barWidth > 0f && total > 0f) {
                                dragValue = (offset.x / barWidth).coerceIn(0f, 1f) * total
                            }
                        },
                        onDragEnd = {
                            if (total > 0f) onSeek(dragValue.toLong())
                            dragging = false
                        },
                        onDragCancel = { dragging = false },
                        onHorizontalDrag = { change, _ ->
                            if (barWidth > 0f && total > 0f) {
                                dragValue = (change.position.x / barWidth)
                                    .coerceIn(0f, 1f) * total
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(0x33FFFFFF))
                )
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AccentBlue, AccentPurple)
                            )
                        )
                )
                if (barWidth > 0f) {
                    Box(
                        Modifier
                            .offset {
                                IntOffset(
                                    x = (progress * barWidth - 4.dp.toPx()).roundToInt()
                                        .coerceAtLeast(0),
                                    y = 0
                                )
                            }
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                fmt(value.toLong()),
                color = Color(0x88FFFFFF),
                fontSize = 11.sp
            )
            Text(
                fmt(duration),
                color = Color(0x88FFFFFF),
                fontSize = 11.sp
            )
        }
    }
}

private fun fmt(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSec = ms / 1000
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}