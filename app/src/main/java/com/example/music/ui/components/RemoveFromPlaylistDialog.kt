package com.example.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.music.data.PlaylistRepository
import com.example.music.data.Song
import com.example.music.data.db.PlaylistEntity
import kotlinx.coroutines.launch

/**
 * 从歌单中移除歌曲。
 * 显示该歌曲所在的所有歌单，点击其中一个即从该歌单移除。
 */
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("从歌单中移除") },
        text = {
            Column {
                Text(
                    "《${song.name}》",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "选择要从哪个歌单移除：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                if (loading) {
                    Text(
                        "加载中…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (playlists.isEmpty()) {
                    Text(
                        "该歌曲不在任何歌单中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                    ) {
                        items(playlists, key = { it.id }) { p ->
                            ListItem(
                                headlineContent = { Text(p.name) },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            repo.remove(p.id, song)
                                            onRemoved("已从「${p.name}」移出")
                                            onDismiss()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}