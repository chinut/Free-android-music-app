package com.example.music.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.music.data.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    index: Int,
    song: Song,
    isCurrent: Boolean,
    isFavorited: Boolean = false,          // ★ 新增：是否已收藏
    showSourceTag: Boolean = true,          // ★ 新增：是否显示源标签
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onFavoriteToggle: () -> Unit,           // 语义变化：收藏/取消收藏
    onShare: () -> Unit = {},
    onArtistClick: (String) -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onShare() }
            )
            .background(
                if (isCurrent) Color(0x338EC5FF)
                else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 序号 / 播放中标记
        Box(
            Modifier
                .size(30.dp)
                .background(
                    if (isCurrent) Color(0xFF8EC5FF)
                    else Color(0x22FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isCurrent) "♪" else "$index",
                color = if (isCurrent) Color(0xFF0A0B10) else Color(0xCCFFFFFF),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.width(12.dp))

        // 歌名 + 歌手·专辑
        Column(Modifier.weight(1f)) {
            Text(
                song.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    append(song.artist.ifBlank { "未知歌手" })
                    if (song.album.isNotBlank()) append(" · ${song.album}")
                },
                color = Color(0x99FFFFFF),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onArtistClick(song.artist) }
            )
        }

        // ★ 源标签（可隐藏）
        if (showSourceTag) {
            Text(
                if (song.source == "tencent") "QQ"
                else if (song.source == "netease") "网易"
                else "—",
                color = Color(0xFF8EC5FF),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // 下载
        IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Download,
                contentDescription = "下载",
                modifier = Modifier.size(18.dp),
                tint = Color(0xCCFFFFFF)
            )
        }

        // ★ 收藏 / 已收藏
        IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isFavorited) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorited) "已收藏" else "收藏",
                modifier = Modifier.size(18.dp),
                tint = if (isFavorited) Color(0xFFFF4D6D) else Color(0xCCFFFFFF)
            )
        }

        // 分享
        IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Share,
                contentDescription = "分享",
                modifier = Modifier.size(18.dp),
                tint = Color(0xCCFFFFFF)
            )
        }

        // 播放
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "播放",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF8EC5FF)
            )
        }
    }
}