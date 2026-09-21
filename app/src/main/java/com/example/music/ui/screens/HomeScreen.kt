package com.example.music.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.music.data.Downloader
import com.example.music.data.Song
import com.example.music.data.share.ShareCodec
import com.example.music.ui.components.ShareDialog
import com.example.music.ui.components.SongRow

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var keyword by remember { mutableStateOf("") }
    var addTarget by remember { mutableStateOf<Song?>(null) }
    var removeTarget by remember { mutableStateOf<Song?>(null) }
    var shareSong by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(Unit) { if (vm.songs.isEmpty()) vm.loadHot() }

    Column(Modifier.fillMaxSize()) {

        // 搜索框
        Box(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = if (isLandscape) 6.dp else 10.dp
                )
                .clip(RoundedCornerShape(50))
                .background(Color(0x22FFFFFF))
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) 38.dp else 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0x99FFFFFF),
                    modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp)
                )
                Spacer(Modifier.width(10.dp))

                Box(
                    Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (keyword.isEmpty()) {
                        Text(
                            "搜索歌曲、歌手或专辑…",
                            color = Color(0x77FFFFFF),
                            fontSize = if (isLandscape) 14.sp else 15.sp
                        )
                    }
                    BasicTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontSize = if (isLandscape) 14.sp else 15.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF8EC5FF)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                val kw = keyword.trim()
                                if (kw.isNotEmpty()) vm.search(kw)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (keyword.isNotEmpty()) {
                    IconButton(
                        onClick = { keyword = "" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除",
                            tint = Color(0x99FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 标题 + 换一批
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (isLandscape) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    vm.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                if (vm.subtitle.isNotBlank() && !isLandscape) {
                    Text(
                        vm.subtitle,
                        color = Color(0x99FFFFFF),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (isLandscape && vm.subtitle.isNotBlank()) {
                Text(
                    vm.subtitle,
                    color = Color(0x99FFFFFF),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            IconButton(onClick = {
                keyword = ""
                vm.loadHot(force = true)
            }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "换一批",
                    tint = Color(0xCCFFFFFF)
                )
            }
        }

        if (vm.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (vm.songs.isEmpty() && !vm.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无歌曲", color = Color(0x99FFFFFF))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(vm.songs, key = { _, s -> "${s.source}:${s.id}" }) { i, song ->
                    val favorited = vm.isFavorited(song)
                    SongRow(
                        index = i + 1,
                        song = song,
                        isCurrent = vm.currentSong?.let {
                            it.id == song.id && it.source == song.source
                        } == true,
                        isFavorited = favorited,
                        showSourceTag = true,
                        onClick = { vm.playAt(i) },
                        onDownload = {
                            Downloader.download(context, song) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFavoriteToggle = {
                            if (favorited) {
                                removeTarget = song
                            } else {
                                addTarget = song
                            }
                        },
                        onShare = { shareSong = song },
                        onArtistClick = { artist ->
                            keyword = artist
                            vm.search(artist)
                        }
                    )
                    HorizontalDivider(
                        color = Color(0x22FFFFFF),
                        thickness = 0.5.dp
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    // 加入歌单弹窗
    addTarget?.let { song ->
        AddToPlaylistDialog(song = song, onDismiss = { addTarget = null })
    }

    // ★ 从歌单移除弹窗
    removeTarget?.let { song ->
        RemoveFromPlaylistDialog(
            song = song,
            onDismiss = { removeTarget = null },
            onRemoved = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 单曲分享弹窗
    shareSong?.let { song ->
        ShareDialog(
            shareText = ShareCodec.encodeSong(song),
            onDismiss = { shareSong = null }
        )
    }
}