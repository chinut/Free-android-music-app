package com.example.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.music.data.PlaylistRepository
import com.example.music.data.share.ShareCodec
import kotlinx.coroutines.launch

/**
 * 导入分享码弹窗。
 * - 歌单：输入新歌单名 → 创建
 * - 单曲：显示歌单列表 → 点击加入
 */
@Composable
fun ImportShareDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onImported: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { PlaylistRepository(context) }

    var input by remember { mutableStateOf(initialText) }
    var decoded by remember { mutableStateOf<ShareCodec.Decoded?>(null) }
    var newName by remember { mutableStateOf("") }

    // 自动识别（如果 initialText 非空）
    remember(initialText) {
        if (initialText.isNotBlank()) {
            val d = ShareCodec.decode(initialText)
            decoded = d
            if (d is ShareCodec.Decoded.Playlist) newName = d.name
        }
        true
    }

    fun parse() {
        val d = ShareCodec.decode(input)
        decoded = d
        if (d is ShareCodec.Decoded.Playlist) newName = d.name
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入分享码") },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        decoded = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("粘贴分享码…") },
                    label = { Text("分享码") }
                )
                Spacer(Modifier.height(12.dp))

                when (val d = decoded) {
                    is ShareCodec.Decoded.Playlist -> {
                        Text("识别为歌单：${d.name}（${d.songs.size} 首）")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("新歌单名称") },
                            singleLine = true
                        )
                    }
                    is ShareCodec.Decoded.Single -> {
                        Text("识别为单曲：${d.song.name}")
                        Text("歌手：${d.song.artist}")
                        Spacer(Modifier.height(8.dp))
                        Text("选择要加入的歌单：")
                        // 简单起见显示提示，让用户去歌单页操作
                        // 完整版可以在这里列出歌单
                    }
                    is ShareCodec.Decoded.Invalid -> {
                        Text("❌ ${d.reason}")
                    }
                    null -> {
                        Text("粘贴后点右下角「识别」")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val d = decoded
                if (d == null) {
                    parse()
                } else {
                    scope.launch {
                        when (d) {
                            is ShareCodec.Decoded.Playlist -> {
                                val finalName = newName.ifBlank { d.name }
                                val id = repo.create(finalName)
                                d.songs.forEach { repo.add(id, it) }
                                onImported("已创建歌单「$finalName」，共 ${d.songs.size} 首")
                                onDismiss()
                            }
                            is ShareCodec.Decoded.Single -> {
                                // 简化处理：加入第一个歌单
                                // 若有需要可改为弹出歌单选择器
                                onImported("请到歌单页面手动添加这首歌")
                                onDismiss()
                            }
                            else -> parse()
                        }
                    }
                }
            }) {
                Text(if (decoded == null) "识别" else "导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}