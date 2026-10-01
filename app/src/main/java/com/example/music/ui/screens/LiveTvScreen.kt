package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.music.data.tv.TvCategory
import com.example.music.data.tv.TvChannel
import com.example.music.data.tv.TvFavorites

/**
 * 在线电视页面。取代原来的“情景模式”。
 *
 * 列表用原生 Compose（分类 + 频道网格），播放交给 [TvPlayerScreen]。
 * 配色与 App 其余页面保持一致（透明背景 + 动态渐变 + 蓝色强调色）。
 */
@Composable
fun LiveTvScreen(
    onPlayingChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    // 支持 -e tvUrl <频道地址> 直接开播某频道
    val autoPlayUrl = remember(context) {
        (context as? android.app.Activity)?.intent?.getStringExtra("tvUrl")
    }
    val app = context.applicationContext as android.app.Application
    val vm: LiveTvViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                LiveTvViewModel(app, autoPlayUrl) as T
        }
    )
    val categories by vm.categories.collectAsState()
    val loading by vm.loading.collectAsState()
    val selected by vm.selectedCategory.collectAsState()
    val playing by vm.playing.collectAsState()

    // 通知外层：播放中要全屏铺满，列表要保留动态渐变背景
    LaunchedEffect(playing) { onPlayingChanged(playing != null) }
    DisposableEffect(Unit) { onDispose { onPlayingChanged(false) } }

    val current = playing
    if (current != null) {
        val sourceState by vm.source.collectAsState()
        val allFailed by vm.allSourcesFailed.collectAsState()
        TvPlayerScreen(
            initialChannel = current,
            allCategories = categories,
            sourceInfo = if (sourceState.total > 1) {
                "源 ${sourceState.index + 1}/${sourceState.total}"
            } else null,
            hasAlternates = sourceState.hasMore,
            allSourcesFailed = allFailed,
            onSourceFailed = { vm.retryWithNextSource() },
            onClose = { vm.stop() },
        )
        return
    }

    var favOnly by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.LiveTv,
                contentDescription = null,
                tint = Color(0xFF8EC5FF),
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "📺 在线电视",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            "央视、卫视与地方台，点击频道即可观看",
            color = Color(0x99FFFFFF),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(10.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8EC5FF))
            }
            return@Column
        }

        if (categories.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("频道数据加载失败", color = Color(0x99FFFFFF))
            }
            return@Column
        }

        val favUrls by TvFavorites.flow.collectAsState()
        // 首次进入时把磁盘上的收藏读进来
        LaunchedEffect(Unit) { TvFavorites.urls(context) }

        // 分类（含“收藏”）
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                CategoryChip(
                    text = "★ 收藏 ${favUrls.size}",
                    selected = favOnly == 1,
                    onClick = { favOnly = 1 },
                )
            }
            items(categories.size) { index ->
                val cat = categories[index]
                CategoryChip(
                    text = "${cat.name} ${cat.channels.size}",
                    selected = favOnly == 0 && index == selected,
                    onClick = {
                        favOnly = 0
                        vm.selectCategory(index)
                    },
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        val channels: List<TvChannel> = if (favOnly == 1) {
            categories.flatMap { it.channels }.filter { favUrls.contains(it.url) }
        } else {
            categories[selected.coerceIn(0, categories.size - 1)].channels
        }

        if (channels.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "还没有收藏频道\n在播放页点右上角的心形收藏",
                    color = Color(0x99FFFFFF),
                    fontSize = 13.sp,
                )
            }
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(channels, key = { it.url + it.name }) { ch ->
                ChannelCell(
                    channel = ch,
                    favorite = favUrls.contains(ch.url),
                    onClick = { vm.play(ch) },
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0x338EC5FF) else Color(0x22FFFFFF))
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) Color(0xFF8EC5FF) else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF8EC5FF) else Color(0xCCFFFFFF),
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChannelCell(
    channel: TvChannel,
    favorite: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1FFFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            channel.name,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (favorite) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF6B9F),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp),
            )
        }
    }
}
