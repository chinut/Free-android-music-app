package com.example.music.ui.screens

import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.music.data.Downloader
import com.example.music.data.PlaylistRepository
import com.example.music.data.Song
import com.example.music.data.share.ShareCodec
import com.example.music.player.PlayerHolder
import com.example.music.ui.components.ShareDialog
import com.example.music.ui.components.SongRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(nav: NavHostController, playlistId: Long) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val repo = remember { PlaylistRepository(context) }
    val scope = rememberCoroutineScope()

    val songs by repo.songs(playlistId).collectAsState(initial = emptyList())
    val playlists by repo.playlists().collectAsState(initial = emptyList())
    val playlistName = playlists.firstOrNull { it.id == playlistId }?.name ?: "歌单"
    val current by PlayerHolder.currentSong.collectAsState()

    var shareText by remember { mutableStateOf<String?>(null) }
    var shareSong by remember { mutableStateOf<Song?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(playlistName, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = {
                    if (songs.isEmpty()) {
                        Toast.makeText(context, "歌单为空", Toast.LENGTH_SHORT).show()
                    } else {
                        shareText = ShareCodec.encodePlaylist(playlistName, songs)
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "分享歌单", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (songs.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = if (isLandscape) 2.dp else 6.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = { PlayerHolder.playQueue(songs, 0) }) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("播放全部")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { PlayerHolder.playShuffle(songs) }) {
                    Icon(Icons.Default.Shuffle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("随机播放")
                }
            }
        }

        if (songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("这个歌单还是空的", color = Color(0x99FFFFFF))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(songs, key = { _, s -> "${s.source}:${s.id}" }) { i, song ->
                    SongRow(
                        index = i + 1,
                        song = song,
                        isCurrent = current?.let {
                            it.id == song.id && it.source == song.source
                        } == true,
                        isFavorited = true,          // ★ 歌单里的都是已收藏
                        showSourceTag = false,        // ★ 不显示源标签
                        onClick = { PlayerHolder.playQueue(songs, i) },
                        onDownload = {
                            Downloader.download(context, song) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        // ★ 点击 ♥️ → 直接移出当前歌单
                        onFavoriteToggle = {
                            scope.launch {
                                repo.remove(playlistId, song)
                                Toast.makeText(
                                    context,
                                    "已从「$playlistName」移出",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onShare = { shareSong = song }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0x22FFFFFF))
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    shareText?.let { text ->
        ShareDialog(shareText = text, onDismiss = { shareText = null })
    }

    shareSong?.let { song ->
        ShareDialog(
            shareText = ShareCodec.encodeSong(song),
            onDismiss = { shareSong = null }
        )
    }
}