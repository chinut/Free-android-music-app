package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch

private val AccentBlue = Color(0xFF6B9FFF)
private val AccentPurple = Color(0xFF8E5BFF)

@Composable
fun AddToPlaylistDialog(song: Song, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { PlaylistRepository(context) }
    val scope = rememberCoroutineScope()

    val playlists by repo.playlists().collectAsState(initial = emptyList())
    val favoritedKeys by repo.allFavoritedKeys().collectAsState(initial = emptySet())

    var newName by remember { mutableStateOf("") }
    // 哪些歌单已经包含这首歌
    val containingIds = remember(playlists, favoritedKeys) {
        playlists.mapNotNull { p ->
            val songKey = "${song.source}:${song.id}"
            // 需要查 DB 才知道这个歌单是否包含，简化做法：单个查询
            null
        }.toSet()
    }

    // 更靠谱的做法：异步查询所有包含这首歌的歌单
    var containedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    androidx.compose.runtime.LaunchedEffect(song.id, song.source) {
        containedIds = repo.findPlaylistsContaining(song).map { it.id }.toSet()
    }

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
                                    colors = listOf(AccentBlue, AccentPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "加入歌单",
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

                // 新建歌单输入
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x22FFFFFF))
                            .border(
                                width = 1.dp,
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        if (newName.isEmpty()) {
                            Text(
                                "新建歌单…",
                                color = Color(0x66FFFFFF),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val n = newName.trim()
                            if (n.isEmpty()) return@IconButton
                            scope.launch {
                                val id = repo.create(n)
                                repo.add(id, song)
                                newName = ""
                                onDismiss()
                            }
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (newName.isNotBlank()) {
                                    Brush.linearGradient(
                                        colors = listOf(AccentBlue, AccentPurple)
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF))
                                    )
                                }
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "创建",
                            tint = if (newName.isNotBlank()) Color.White else Color(0x66FFFFFF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "还没有歌单，先创建一个吧",
                            color = Color(0x88FFFFFF),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        "选择歌单",
                        color = Color(0x99FFFFFF),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(playlists, key = { it.id }) { p ->
                            val already = p.id in containedIds
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x11FFFFFF))
                                    .clickable {
                                        if (already) {
                                            scope.launch {
                                                repo.remove(p.id, song)
                                                onDismiss()
                                            }
                                        } else {
                                            scope.launch {
                                                repo.add(p.id, song)
                                                onDismiss()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (already) AccentBlue else Color(0x99FFFFFF),
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
                                if (already) {
                                    Text(
                                        "已加入",
                                        color = AccentBlue,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 取消按钮
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