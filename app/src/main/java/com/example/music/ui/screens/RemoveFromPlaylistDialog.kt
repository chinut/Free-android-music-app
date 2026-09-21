package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.music.data.PlaylistRepository
import com.example.music.data.Song
import com.example.music.data.db.PlaylistEntity
import kotlinx.coroutines.launch

private val WarnRed = Color(0xFFFF6B6B)

@Composable
fun RemoveFromPlaylistDialog(
    song: Song,
    onDismiss: () -> Unit,
    onRemoved: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { PlaylistRepository(context) }

    var playlists by remember { mutableStateOf<List<PlaylistEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(song.id, song.source) {
        loading = true
        playlists = repo.findPlaylistsContaining(song)
        loading = false
    }

    // ★ 关键：使用 Dialog + 完全自定义 UI，不要用 AlertDialog
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF01A1240),
                            Color(0xF00A0B10)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                )
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
                                Brush.linearGradient(
                                    colors = listOf(
                                        WarnRed.copy(alpha = 0.9f),
                                        Color(0xFFFF9A6B)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.HeartBroken,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "从歌单中移除",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            song.name,
                            color = Color(0x99FFFFFF),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                when {
                    loading -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "加载中…",
                                color = Color(0x88FFFFFF),
                                fontSize = 13.sp
                            )
                        }
                    }
                    playlists.isEmpty() -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "该歌曲不在任何歌单中",
                                color = Color(0x88FFFFFF),
                                fontSize = 13.sp
                            )
                        }
                    }
                    else -> {
                        Text(
                            "选择要从哪个歌单移除",
                            color = Color(0x99FFFFFF),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            items(playlists, key = { it.id }) { p ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x11FFFFFF))
                                        .clickable {
                                            scope.launch {
                                                repo.remove(p.id, song)
                                                onRemoved("已从「${p.name}」移出")
                                                onDismiss()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFF8EC5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        p.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "移除",
                                        tint = WarnRed.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 取消
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .clickable { onDismiss() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "取消",
                        color = Color(0x99FFFFFF),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}