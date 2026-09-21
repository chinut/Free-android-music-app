package com.example.music.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.music.data.PlaylistRepository
import com.example.music.data.db.PlaylistEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(nav: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val repo = remember { PlaylistRepository(context) }
    val scope = rememberCoroutineScope()
    val playlists by repo.playlists().collectAsState(initial = emptyList())

    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<PlaylistEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PlaylistEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = if (isLandscape) 8.dp else 16.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                " 我的歌单",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showCreate = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建歌单",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "还没有歌单，点右上角 + 新建一个",
                    color = Color(0x99FFFFFF)
                )
            }
        } else {
            if (isLandscape) {
                // 横屏：两列网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(playlists, key = { it.id }) { p ->
                        PlaylistCard(
                            p = p,
                            onOpen = { nav.navigate("playlists/${p.id}") },
                            onRename = {
                                renameTarget = p
                                renameText = p.name
                            },
                            onDelete = { deleteTarget = p }
                        )
                    }
                }
            } else {
                // 竖屏：单列列表
                LazyColumn(Modifier.fillMaxSize()) {
                    items(playlists, key = { it.id }) { p ->
                        ListItem(
                            headlineContent = { Text(p.name, color = Color.White) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        renameTarget = p
                                        renameText = p.name
                                    }) {
                                        Icon(Icons.Default.Edit, "重命名", tint = Color(0xCCFFFFFF))
                                    }
                                    IconButton(onClick = { deleteTarget = p }) {
                                        Icon(Icons.Default.Delete, "删除", tint = Color(0xCCFFFFFF))
                                    }
                                    IconButton(onClick = { nav.navigate("playlists/${p.id}") }) {
                                        Icon(Icons.Default.PlayArrow, "打开", tint = Color(0xFF8EC5FF))
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { nav.navigate("playlists/${p.id}") }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0x33FFFFFF))
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("新建歌单") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("歌单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        scope.launch {
                            repo.create(newName)
                            newName = ""
                            showCreate = false
                        }
                    }
                ) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("取消") }
            }
        )
    }

    renameTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名歌单") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        scope.launch {
                            repo.rename(p.id, renameText)
                            renameTarget = null
                        }
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除歌单") },
            text = { Text("确定删除「${p.name}」？其中的歌曲记录也会一并删除。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repo.delete(p.id)
                        deleteTarget = null
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    p: PlaylistEntity,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0x22FFFFFF)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                p.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, "重命名", tint = Color(0xCCFFFFFF), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "删除", tint = Color(0xCCFFFFFF), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onOpen, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.PlayArrow, "打开", tint = Color(0xFF8EC5FF), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}